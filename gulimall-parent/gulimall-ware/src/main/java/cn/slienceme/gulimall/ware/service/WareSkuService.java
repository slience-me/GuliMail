package cn.slienceme.gulimall.ware.service;

import cn.slienceme.gulimall.ware.vo.SkuHasStockVo;
import cn.slienceme.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 21:07:08
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    List<SkuHasStockVo> getSkuHasStocks(List<Long> ids);

    List<WareSkuLockVo> orderLockStock(WareSkuLockVo itemVos);

//    Boolean orderLockStock(WareSkuLockVo lockVo);

//    void unlock(StockLockedTo stockLockedTo);
//
//    void unlock(OrderTo orderTo);
}

