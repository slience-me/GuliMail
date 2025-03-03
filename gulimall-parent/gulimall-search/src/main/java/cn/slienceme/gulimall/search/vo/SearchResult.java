package cn.slienceme.gulimall.search.vo;

import cn.slienceme.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    private Integer pageNum;    // 当前页码
    private Long total;         // 总记录数
    private Integer totalPages;    // 总页码数

    private List<BrandVo> brands;    // 品牌信息
    private List<AttrVo> attrs;    // 属性信息
    private List<CatalogVo> catalogs;    // 分类信息

    //===========================以上是返回给页面的所有信息============================//

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String logoUrl;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catelogId;
        private String catelogName;
    }
}
