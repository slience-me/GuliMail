package cn.slienceme.gulimall.search.service.impl;

import cn.slienceme.common.to.es.SkuEsModel;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.search.feign.ProductFeignService;
import cn.slienceme.gulimall.search.config.GulimallElasticSearchConfig;
import cn.slienceme.gulimall.search.constant.EsConstant;
import cn.slienceme.gulimall.search.service.MallSearchService;
import cn.slienceme.gulimall.search.vo.AttrResponseVo;
import cn.slienceme.gulimall.search.vo.BrandVo;
import cn.slienceme.gulimall.search.vo.SearchParam;
import cn.slienceme.gulimall.search.vo.SearchResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    // es检索
    @Override
    public SearchResult search(SearchParam params) {
        // 1. 动态构建出查询需要的DSL语句
        SearchResult result = null;
        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(params);

        try {
            // 2. 执行检索请求，获取响应结果
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            // 3. 解析响应结果，封装成我们需要的格式
            result = buildSearchResult(params, response);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return result;
    }

    /**
     * 构建结果请求
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchParam params, SearchResponse response) {
        SearchResult result = new SearchResult();
        // 1. 封装所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> products = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                // 获取检索结果中的_source
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(params.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].toString();
                    esModel.setSkuTitle(skuTitleValue);
                }
                products.add(esModel);
            }
        }
        result.setProducts(products);


        // 2. 封装当前查询到的所有属性信息
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1. 得到属性的ID
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            // 2. 得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            // 3. 得到属性的所有可选值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        // 3. 封装当前查询到的所有品牌信息
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets_brand = brandAgg.getBuckets();
        for (Terms.Bucket bucket : buckets_brand) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 1. 获取品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 2. 得到品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            // 3. 得到品牌的图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setLogoUrl(brand_img_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4. 封装当前查询到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            catalogVo.setCatelogId(Long.parseLong(bucket.getKeyAsString()));
            // 得到分类名称
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatelogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5. 封装分页信息
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? ((int) total / EsConstant.PRODUCT_PAGESIZE) : (((int) total / EsConstant.PRODUCT_PAGESIZE) + 1);
        result.setPageNum(params.getPageNum());
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6. 构建面包屑导航功能
        if (params.getAttrs() != null && params.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = params.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attrs=2_2寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                // 跳转
                String replace = getTargetUrl(params, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html" + (replace.isEmpty() ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }

        // 品牌 分类
        if (params.getBrandId() != null && params.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            // TODO: 远程查询所有品牌
            R r = productFeignService.brandsInfos(params.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer stringBuffer = new StringBuffer();
                String replace="";
                for (BrandVo brandVo : brand) {
                    stringBuffer.append(brandVo.getName());
                    replace = getTargetUrl(params, brandVo.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html" + (replace.isEmpty() ? "" : "?" + replace));
            }
            navs.add(navVo);
        }
        // TODO 不需要导航
        return result;
    }

    private static String getTargetUrl(SearchParam params, String value, String key) {
        try {
            URI encodeData = new URI(null, null, value, null);
            return params.get_queryString().replace("&" + key + "=" + encodeData.toASCIIString(), "")
                    .replace("attrs=" + value + "&", "")
                    .replace("attrs=" + value, "");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 准备检索请求
     * # 聚合分析 过滤(按照属性，分类，品牌，价格区间，库存)，排序，分页，高亮，聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam params) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // 构建DSL语句

        // 过滤 (按照属性，分类，品牌，价格区间，库存)，
        // 1、构建bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 bool - must - 模糊匹配
        if (!StringUtils.isEmpty(params.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", params.getKeyword()));
        }
        // 1.2 bool - filter - 按照三级分类id查询
        if (params.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", params.getCatalog3Id()));
        }
        // 1.2 bool - filter - 按照品牌id查询
        if (params.getBrandId() != null && !params.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", params.getBrandId()));
        }
        // 1.2 bool - filter - 按照属性查询
        if (params.getAttrs() != null && !params.getAttrs().isEmpty()) {
            // attrs=1_5寸:8寸&attrs=2_16G:32G
            for (String attr : params.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attrs=1_5寸:8寸 => [1,5寸:8寸]
                String[] s = attr.split("_");
                String attrId = s[0];  // 检索的属性id
                String[] attrValues = s[1].split(":"); // 检索的属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        // 1.2 bool - filter - 按照是否有库存查询
        if (params.getHasStock() != null) {  // hasStock=0/1
            boolQuery.filter(QueryBuilders.termQuery("hasStock", params.getHasStock() == 1));
        }
        // 1.2 bool - filter - 按照价格区间查询
        if (!StringUtils.isEmpty(params.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = params.getSkuPrice().split("_");
            if (s.length == 1) {
                if (params.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(Integer.parseInt(s[0]));
                }
                if (params.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(Integer.parseInt(s[0]));
                    rangeQuery.lte(30000);
                }
            } else if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
                //_6000会截取成["","6000"]
                if (!s[0].isEmpty()) {
                    rangeQuery.gte(Integer.parseInt(s[0]));
                }
                rangeQuery.lte(Integer.parseInt(s[1]));
            }
            boolQuery.filter(rangeQuery);
        }

        // 查询
        sourceBuilder.query(boolQuery);

        // 排序，分页，高亮
        // 2.1 sort - 排序 sort=price/salecount/hotscore_desc/asc
        if (!StringUtils.isEmpty(params.getSort())) {
            String sort = params.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        // 2.2 分页 from=0&size=5
        // pageNum: from:0 size:5 [0,1,2,3,4]
        // pageNum: from:5 size:5 [5,6,7,8,9]
        sourceBuilder.from((params.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 2.3 高亮
        if (!StringUtils.isEmpty(params.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red;'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        /*
         * 聚合分析
         * */
        // 3.1 按照品牌id查询

        // 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        // 3.2 按照分类id查询
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        // 3.3 按照属性查询
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");

        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        String s = sourceBuilder.toString();
        System.out.println("ES:" + s);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
    }
}
