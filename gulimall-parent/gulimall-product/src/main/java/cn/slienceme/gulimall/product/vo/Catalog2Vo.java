package cn.slienceme.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


// 二级分类vo
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {

    private String catalog1Id;  // 一级父分类
    private List<Catalog3Vo> catalog3List; // 三级子分类
    private String id;
    private String name;

    // 三级分类Vo

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id;  // 2级父分类
        private String id;
        private String name;
    }
}

