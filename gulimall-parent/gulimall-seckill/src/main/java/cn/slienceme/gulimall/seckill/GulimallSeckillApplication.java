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
 * 3) 配置 application.yml 配置sentinel控制台地址信息
 * 4) 在控制台调整参数【默认所有的设置保存在内存中，重启失效】
 * 2、每一个微服务都导入actuator模块  实时监控模块
 *      配置management:endpoints:web:exposure:include: '*'
 * 3、自定义返回数据
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
