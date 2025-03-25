package cn.slienceme.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合Sentinel
 * 1) 引入依赖
 * 2) 下载sentinel控制台
 * 3) 配置 application.properties 配置sentinel控制台地址信息
 * 4) 在控制台调整参数【默认所有的设置保存在内存中，重启失效】
 * 2、每一个微服务都导入actuator模块  实时监控模块
 *      配置management:endpoints:web:exposure:include: '*'
 * 3、自定义返回数据
 *
 * 4 使用Sentinel来保护feign远程调用：熔断
 *  1）调用方熔断保护 加配置 feign.sentinel.enabled=true
 *  2）手动指定远程服务的降级策略  远程服务被降级处理
 *  3）超大流量的时候  必须牺牲一些远程服务，在服务的提供方（远程服务）指定降级策略
 *      提供方是在运行 但是不运行自己的业务逻辑 返回的是默认的降级数据（限流的数据）
 *
 * 5、自定义异常处理
 *
 *   1)  代码方式 try(Entry entry = SphU.entry("SeckillSkus")) {
 *           } catch (BlockException e) {
 *          }
 *   2） 注解方式 @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "handlerException")
 *
 *   无论哪一种方式，都要确定要返回什么数据
 *
 */

@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = "cn.slienceme.gulimall.seckill.feign")
@EnableRedisHttpSession
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
