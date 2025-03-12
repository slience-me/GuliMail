package cn.slienceme.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuHasStockInner {
    private Long skuId;
    private Integer num;
    private List<Long> wareIds;
}
