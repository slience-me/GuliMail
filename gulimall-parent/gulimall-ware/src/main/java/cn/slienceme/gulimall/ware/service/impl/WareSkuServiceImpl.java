package cn.slienceme.gulimall.ware.service.impl;

import cn.slienceme.common.constant.OrderStatusEnum;
import cn.slienceme.common.exception.NoStockException;
import cn.slienceme.common.to.OrderTo;
import cn.slienceme.common.to.StockDetailTo;
import cn.slienceme.common.to.StockLockedTo;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.ware.constant.WareTaskStatusEnum;
import cn.slienceme.gulimall.ware.entity.WareOrderTaskDetailEntity;
import cn.slienceme.gulimall.ware.entity.WareOrderTaskEntity;
import cn.slienceme.gulimall.ware.feign.OrderFeignService;
import cn.slienceme.gulimall.ware.feign.ProductFeignService;
import cn.slienceme.gulimall.ware.service.WareOrderTaskDetailService;
import cn.slienceme.gulimall.ware.service.WareOrderTaskService;
import cn.slienceme.gulimall.ware.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.ware.dao.WareSkuDao;
import cn.slienceme.gulimall.ware.entity.WareSkuEntity;
import cn.slienceme.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId)
                .eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> list = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuId(skuId);
            // 查询库存
            // SELECT SUM(stock-stock_locked) FROM wms_ware_sku WHERE sku_id=1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStocks(List<Long> ids) {
        List<SkuHasStockVo> skuHasStockVos = ids.stream().map(id -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(id);
            Integer count = baseMapper.getTotalStock(id);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    @Transactional
    @Override
    public List<WareSkuLockVo> orderLockStock(WareSkuLockVo wareSkuLockVo) {

        //因为可能出现订单回滚后，库存锁定不回滚的情况，但订单已经回滚，得不到库存锁定信息，因此要有库存工作单
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);

        // 找到每个商品在哪个仓库都有库存
        List<OrderItemVo> itemVos = wareSkuLockVo.getLocks();
        List<SkuHasStockInner> lockVos = itemVos.stream().map((item) -> {
            SkuHasStockInner skuLockVo = new SkuHasStockInner();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setNum(item.getCount());
            //找出所有库存大于商品数的仓库
            List<Long> wareIds = baseMapper.listWareIdsHasStock(item.getSkuId(), item.getCount());
            skuLockVo.setWareIds(wareIds);
            return skuLockVo;
        }).collect(Collectors.toList());
        Boolean allLock = true;
        // 去锁定库存
        for (SkuHasStockInner lockVo : lockVos) {
            boolean lock = true;
            Long skuId = lockVo.getSkuId();
            List<Long> wareIds = lockVo.getWareIds();
            //如果没有满足条件的仓库，抛出异常
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品  库存不足
                throw new NoStockException(skuId);
            } else {
                // 遍历仓库信息
                for (Long wareId : wareIds) {
                    // 成功就返回1 否则就是0
                    Long count = wareSkuDao.lockWareSku(skuId, lockVo.getNum(), wareId);
                    if (count == 0) {
                        // 当前仓库锁失败
                        lock = false;
                    } else {
                        //锁定成功，保存工作单详情
                        WareOrderTaskDetailEntity detailEntity = WareOrderTaskDetailEntity.builder()
                                .skuId(skuId)
                                .skuName("")
                                .skuNum(lockVo.getNum())
                                .taskId(taskEntity.getId())
                                .wareId(wareId)
                                .lockStatus(1).build();
                        wareOrderTaskDetailService.save(detailEntity);
                        //发送库存锁定消息至延迟队列
                        StockLockedTo lockedTo = new StockLockedTo();
                        lockedTo.setId(taskEntity.getId());
                        StockDetailTo detailTo = new StockDetailTo();
                        BeanUtils.copyProperties(detailEntity, detailTo);
                        lockedTo.setDetailTo(detailTo);
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                        lock = true;
                        break;
                    }
                }
            }
            if (!lock) {
                throw new NoStockException(skuId);
            }
        }
        return null;
    }

    @Override
    public void unlockStock(StockLockedTo to) {

        System.out.println("收到库存解锁的消息");
        Long Id = to.getId(); // 库存工作单id
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();
        // 解锁
        // 1、查询数据库关于这个订单的锁定库存信息
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailId);
        // 没有这个库存工作单信息，说明库存锁定成功，无需解锁
        if (detailEntity != null) {
            // 2、有 => 证明库存锁定成功
            //        如果没有这个订单 必须 解锁
            //             有这个订单 不能解锁库存  必须去订单表查看订单状态
            //                      已取消 =》 可以解锁
            //                      没有取消 =》 不能解锁
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(Id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo order = r.getData(new TypeReference<OrderVo>() {
                });
                //没有这个订单||订单状态已经取消 解锁库存
                if (order == null || order.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    //为保证幂等性，只有当工作单详情处于被锁定的情况下才进行解锁
                    if (detailEntity.getLockStatus() == WareTaskStatusEnum.Locked.getCode()) {
                        unlockStock(detail.getSkuId(), detail.getSkuNum(), detail.getWareId(), detailEntity.getId());
                    }
                }
            } else {
                // 消息拒绝后重新入队继续让别人解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁
        }
    }
    @Transactional
    @Override
    public void unlockOrder(OrderTo orderTo) {
        //为防止重复解锁，需要重新查询工作单
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getBaseMapper().selectOne((new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn)));
        //查询出当前订单相关的且处于锁定状态的工作单详情
        List<WareOrderTaskDetailEntity> lockDetails = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()).eq("lock_status", WareTaskStatusEnum.Locked.getCode()));
        for (WareOrderTaskDetailEntity lockDetail : lockDetails) {
            unlockStock(lockDetail.getSkuId(), lockDetail.getSkuNum(), lockDetail.getWareId(), lockDetail.getId());
        }
    }

    private void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        //数据库中解锁库存数据
        baseMapper.unlockStock(skuId, skuNum, wareId);
        //更新库存工作单详情的状态
        WareOrderTaskDetailEntity detail = WareOrderTaskDetailEntity.builder()
                .id(detailId)
                .lockStatus(2).build();
        wareOrderTaskDetailService.updateById(detail);
    }


}
