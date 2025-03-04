package cn.slienceme.gulimall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递的查询条件
 */
@Data
public class SearchParam {
    private String keyword; //页面传递过来的全文匹配关键字
    private Long catalog3Id; //三级分类id

    /**
     * sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * hasStock=0/1
     */
    private Integer hasStock; //是否显示有货

    /**
     * skuPrice=1_500/_500/500_
     */
    private String skuPrice; //价格区间查询

    /**
     * brandId=1
     */
    private List<Long> brandId; //品牌id,可以多选

    /**
     * attrs=1_5寸:8寸&2_16G:32G
     */
    private List<String> attrs; //按照属性进行筛选

    private Integer pageNum = 1; //页码

    private String _queryString; //原生的所有查询条件

    //===========================以上是返回给页面的所有信息============================//


//    /* 面包屑导航数据 */
//    private List<NavVo> navs;
//
//    @Data
//    public static class NavVo {
//        private String navName;
//        private String navValue;
//        private String link;
//    }
}
