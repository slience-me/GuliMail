package cn.slienceme.gulimall.search;

import cn.slienceme.gulimall.search.config.GulimallElasticSearchConfig;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)  // 使用SpringRunner运行
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest ("users");
        indexRequest.id("1");
        indexRequest.source("userName", "张三", "age", 20, "gender", "男");

        User user = new User();
        user.setUsername("张三");
        user.setAge(20);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        //设置要保存的内容
        indexRequest.source(jsonString, XContentType.JSON);
        //执行创建索引和保存数据
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }


    /**
     * 复杂检索:在bank中搜索address中包含mill的所有人的年龄分布以及平均年龄，平均薪资
     * @throws IOException
     */
    @Test
    public void searchData() throws IOException {
        //1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();

        //1.1）指定索引
        searchRequest.indices("bank");
        //1.2）构造检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address","Mill"));

        //1.2.1)按照年龄分布进行聚合
        TermsAggregationBuilder ageAgg=AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        //1.2.2)计算平均年龄
        AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
        sourceBuilder.aggregation(ageAvg);
        //1.2.3)计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件："+sourceBuilder);
        searchRequest.source(sourceBuilder);
        //2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("检索结果："+searchResponse);

        //3. 将检索结果封装为Bean
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);

        }

        //4. 获取聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄："+keyAsString+" ==> "+bucket.getDocCount());
        }
        Avg ageAvg1 = aggregations.get("ageAvg");
        System.out.println("平均年龄："+ageAvg1.getValue());

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资："+balanceAvg1.getValue());
    }

    /**
     * (1) 方便检索 {
     *     skuId: 1,
     *     spuId: 1,
     *     skuTitle: "华为",
     *     price: 100,
     *     saleCount: 100,
     *     attr: [
     *         {尺寸：5寸},{CPU：高通945},{分辨率：全高清}
     *        ]
     * }
     * 冗余
     * 100万*20=1000000*2KB=2000MB=2GB
     *
     * (2) 索引库结构
     *  sku索引 {
     *      skuId: 1,
     *      spuId: 11,
     *      xxxxx
     *  }
     *  attr索引 {
     *      spuId: 11,
     *      attr: [
     *          {尺寸：5寸},{CPU：高通945},{分辨率：全高清}
     *      ]
     *  }
     *
     *  搜索小米：杂粮类、手机类、电器类
     *  10000个， 4000个spu
     *  分步查询，10000个 -> 4000个spu -> 对应的attr
     *  esClient：spuId: [4000个spuid] 4000*8=32000字节=32KB
     *
     *  32kb*10000=320000KB=320MB
     *  32kb*1000000=320000000KB=320GB
     *
     *  空间与时间的权衡
     *    *  100万*20=1000000*2KB=2000MB=2GB
     *    *  32kb*1000000=320000000KB=320GB
     *
     *    方案1 ：冗余，空间换时间
     *    方案2 ：倒排索引，时间换空间
     */


    @Setter
    @Getter
    class User{
        private int age;
        private String username;
        private String gender;
    }

    @ToString
    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
}
