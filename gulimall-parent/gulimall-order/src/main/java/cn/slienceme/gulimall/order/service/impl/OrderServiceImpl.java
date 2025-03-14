package cn.slienceme.gulimall.order.service.impl;

import cn.slienceme.common.exception.NoStockException;
import cn.slienceme.common.to.OrderTo;
import cn.slienceme.common.to.SeckillOrderTo;
import cn.slienceme.common.utils.R;
import cn.slienceme.common.vo.MemberRespVo;
import cn.slienceme.common.constant.OrderStatusEnum;
import cn.slienceme.gulimall.order.constant.PayConstant;
import cn.slienceme.gulimall.order.entity.OrderItemEntity;
import cn.slienceme.gulimall.order.entity.PaymentInfoEntity;
import cn.slienceme.gulimall.order.feign.CartFeignService;
import cn.slienceme.gulimall.order.feign.MemberFeignService;
import cn.slienceme.gulimall.order.feign.ProductFeignService;
import cn.slienceme.gulimall.order.feign.WareFeignService;
import cn.slienceme.gulimall.order.interceptor.LoginUserInterceptor;
import cn.slienceme.gulimall.order.service.OrderItemService;
import cn.slienceme.gulimall.order.service.PaymentInfoService;
import cn.slienceme.gulimall.order.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.order.dao.OrderDao;
import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import cn.slienceme.common.to.SkuHasStockTo;
import cn.slienceme.gulimall.order.constant.OrderConstant;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        // 获取当前登录用户信息
        MemberRespVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 解决 线程安全问题 使用异步 线程池
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> itemAndStockFuture = CompletableFuture.supplyAsync(() -> {
            // 解决 线程安全问题 使用异步 线程池
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 查出所有选中购物项
            List<OrderItemVo> checkedItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(checkedItems);
            return checkedItems;
        }, executor).thenAcceptAsync((items) -> {
            // 库存
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            //skuId为key,是否有库存为value
            R hasStock = wareFeignService.getSkuHasStock(skuIds);
            List<SkuHasStockTo> data = hasStock.getData(new TypeReference<List<SkuHasStockTo>>() {
            });
            if (data != null && data.size() > 0) {
                Map<Long, Boolean> map = data.stream()
                        .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);

        // 查出所有收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 当前登录用户地址信息
            List<MemberAddressVo> addressByUserId = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addressByUserId);
        }, executor);

        // 查询用户积分
        confirmVo.setIntegration(memberResponseVo.getIntegration());

        // 总价自动计算
        // 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        try {
            CompletableFuture.allOf(itemAndStockFuture, addressFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return confirmVo;
    }

    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        OrderSubmitVo submitVo = confirmVoThreadLocal.get();

        //1) 设置用户信息
        orderEntity.setMemberId(respVo.getId());
        orderEntity.setMemberUsername(respVo.getUsername());

        //2) 获取邮费和收件人信息并设置
        R fareRes = wareFeignService.getFare(submitVo.getAddrId());
        FareVo fareVo = fareRes.getData(new TypeReference<FareVo>() {
        });
        // 费用
        orderEntity.setFreightAmount(fareVo.getFare());
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //3) 设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //2) 设置sku相关属性
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";"));
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        //3) 通过skuId查询spu相关属性并设置
        R r = productFeignService.getSpuBySkuId(item.getSkuId());
        if (r.getCode() == 0) {
            SpuInfoTo spuInfo = r.getData(new TypeReference<SpuInfoTo>() {
            });
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            orderItemEntity.setSpuBrand(spuInfo.getBrandName());
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        }
        //4) 商品的优惠信息(不做)

        //5) 商品的积分成长，为价格x数量
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

        //6) 订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //7) 实际价格 = 原价 - 优惠 - 优惠券
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> checkedItems = cartFeignService.getCurrentUserCartItems();
        List<OrderItemEntity> orderItemEntities = checkedItems.stream().map((item) -> {
            OrderItemEntity orderItemEntity = buildOrderItem(item);
            //1) 设置订单号
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;
    }

    private void compute(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        // 总价
        BigDecimal total = BigDecimal.ZERO;

        // 优惠价格
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");

        // 积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        entity.setTotalAmount(total);
        entity.setPromotionAmount(promotion);
        entity.setIntegrationAmount(integration);
        entity.setCouponAmount(coupon);
        entity.setIntegration(integrationTotal);
        entity.setGrowth(growthTotal);

        // 付款价格 = 商品价格 + 运费
        entity.setPayAmount(entity.getFreightAmount().add(total));

        // 设置删除状态 (0-未删除，1-已删除)
        entity.setDeleteStatus(0);
    }

    private OrderCreateTo createOrderTo(MemberRespVo memberResponseVo) {
        // 2.1 生成订单号
        String orderSn = IdWorker.getTimeId();
        // 2.2 构建订单号
        OrderEntity entity = buildOrder(orderSn);
        // 2.3 构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        // 2.4 计算价格
        compute(entity, orderItemEntities);
        OrderCreateTo createTo = new OrderCreateTo();
        createTo.setOrder(entity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }

    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        this.save(order);
        // seata不支持
        // orderItemService.saveBatch(orderCreateTo.getOrderItems());
        for (OrderItemEntity orderItem : orderCreateTo.getOrderItems()) {
            orderItemService.save(orderItem); // 逐条插入
        }

    }

    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        confirmVoThreadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        //1. 验证防重令牌
        MemberRespVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), submitVo.getOrderToken());
        // 1-删除失败 0-删除成功
        if (execute == 0L) {
            //1.1 防重令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //2. 创建订单、订单项
            OrderCreateTo order = createOrderTo(memberResponseVo);

            //3. 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // TODO: 4. 保存订单
                saveOrder(order);
                // TODO: 5. 锁定库存
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    return orderItemVo;
                }).collect(Collectors.toList());
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(orderItemVos);
                // TODO: 远程锁定库存
                R r = wareFeignService.orderLockStock(lockVo);
                //5.1 锁定库存成功
                if (r.getCode() == 0) {
                    responseVo.setOrder(order.getOrder());
                    responseVo.setCode(0);

                    //发送消息到订单延迟队列，判断过期订单
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    // TODO: 远程扣减积分
                    //清除购物车记录
                    /*BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + memberResponseVo.getId());
                    for (OrderItemEntity orderItem : order.getOrderItems()) {
                        ops.delete(orderItem.getSkuId().toString());
                    }*/
                    return responseVo;
                } else {
                    //5.1 锁定库存失败 事务回滚
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            } else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭过期的的订单
     *
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //因为消息发送过来的订单已经是很久前的了，中间可能被改动，因此要查询最新的订单
        OrderEntity newOrderEntity = this.getById(orderEntity.getId());
        //如果订单还处于新创建的状态，说明超时未支付，进行关单
        if (newOrderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(newOrderEntity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);

            //关单后发送消息通知其他服务进行关单相关的操作，如解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(newOrderEntity, orderTo);
            try {
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            }catch (Exception e){
                //TODO 重发
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderStatus = this.getOrderStatus(orderSn);
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);
        BigDecimal bigDecimal = orderStatus.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(bigDecimal.toString());
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setSubject(order_sn.get(0).getSkuName());
        payVo.setBody("谷粒商城测试订单");
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> orderList = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> orderItemEntityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemsEntities(orderItemEntityList);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(orderList);

        return new PageUtils(page);
    }

    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        String orderSn = payAsyncVo.getOut_trade_no();
        infoEntity.setOrderSn(orderSn);
        infoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        infoEntity.setSubject(payAsyncVo.getSubject());
        String trade_status = payAsyncVo.getTrade_status();
        infoEntity.setPaymentStatus(trade_status);
        infoEntity.setCreateTime(new Date());
        infoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(infoEntity);

        //判断交易状态是否成功
        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode(), PayConstant.ALIPAY);
        }
    }

    @Transactional
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        MemberRespVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        //1. 创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        if (memberResponseVo!=null){
            orderEntity.setMemberUsername(memberResponseVo.getUsername());
        }
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setCreateTime(new Date());
        orderEntity.setPayAmount(orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum())));
        this.save(orderEntity);
        //2. 创建订单项
        R r = productFeignService.info(orderTo.getSkuId());
        if (r.getCode() == 0) {
            SeckillSkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SeckillSkuInfoVo>() {
            });
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderTo.getOrderSn());
            orderItemEntity.setSpuId(skuInfo.getSpuId());
            orderItemEntity.setCategoryId(skuInfo.getCatalogId());
            orderItemEntity.setSkuId(skuInfo.getSkuId());
            orderItemEntity.setSkuName(skuInfo.getSkuName());
            orderItemEntity.setSkuPic(skuInfo.getSkuDefaultImg());
            orderItemEntity.setSkuPrice(skuInfo.getPrice());
            orderItemEntity.setSkuQuantity(orderTo.getNum());
            orderItemService.save(orderItemEntity);
        }
    }

}
