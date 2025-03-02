package cn.slienceme.gulimall.product;

import cn.slienceme.gulimall.product.entity.BrandEntity;
import cn.slienceme.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功...");
    }

    @Test
    public void test() {
        System.out.println("测试");
    }

    @Autowired
    StringRedisTemplate redisTemplate;
    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("hello","world_"+ UUID.randomUUID());

        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Autowired
    RedissonClient redissonClient;
    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }


}
