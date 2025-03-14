package cn.slienceme.gulimall.order.service;

import cn.slienceme.common.to.SeckillOrderTo;
import cn.slienceme.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 21:08:41
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    void handlerPayResult(PayAsyncVo payAsyncVo);

    void createSeckillOrder(SeckillOrderTo orderTo);
}

