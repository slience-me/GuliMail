package cn.slienceme.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {

    private Long skuId; // skuId
    private Long spuId; // spuId
    private String skuTitle; // 标题
    private BigDecimal skuPrice; // 价格
    private String skuImg; // 图片
    private Long saleCount; // 销量
    private Boolean hasStock; // 是否有库存
    private Long hotScore; // 热度评分
    private Long brandId; // 品牌id
    private Long catalogId; // 分类id
    private String brandName; // 品牌名
    private String brandImg; // 品牌图片
    private String catalogName; // 分类名
    private List<Attr> attrs; // 商品属性

    /**
     *  定义一个静态内部类Attr
     */
    @Data
    public static class Attr{
        private Long attrId; // 属性id
        private String attrName; // 属性名
        private String attrValue; // 属性值
    }
}
