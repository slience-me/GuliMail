package cn.slienceme.gulimall.order.vo;

import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice; // 订单计算的应付价格
    private BigDecimal fare; // 运费
}
