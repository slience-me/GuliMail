package cn.slienceme.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId; // 收获地址的id
    private Integer payType; // 支付方式

    //无需提交要购买的商品，去购物车再获取一遍
    //优惠、发票

    private String orderToken; // 防重令牌
    private BigDecimal payPrice; // 应付价格
    private String remarks; // 订单备注

    //用户相关的信息，直接去session中取出即可
}
