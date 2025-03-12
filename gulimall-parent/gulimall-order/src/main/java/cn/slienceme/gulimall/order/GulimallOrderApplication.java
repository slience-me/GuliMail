package cn.slienceme.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 使用RabbitMQ
 * 1. 引入amqp场景：RabbitAutoConfiguration自动生效
 * 2. 给容器中自动配置了连接工厂，RabbitTemplate、AmqpAdmin、RabbitMessagingTemplate
 *      所有的属性都是 spring.rabbitmq
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 * 3. 给配置文件中配置 spring.rabbitmq 信息
 * 4. @EnableRabbit开启基于注解的RabbitMQ模式
 * 5. 监听消息 @RabbitListener 必须有 @EnableRabbit
 *     @RabbitListener: 类+方法上 (监听哪些队列即可) 因为 底层是@EventListener
 *     @RabbitHandler: 方法上 (重载区分不同的消息)
 *     @RabbitListener可以有多个，@RabbitHandler只能有一个
 *
 * 事务失效问题：
 * 本地事务问题
 *      // 同一个对象内事务方法互调默认失效，原因 绕过了代理对象 事务使用代理对象来控制的
 *      // 1. 引入aop-starter；spring-boot-starter-aop；引入了aspectj；底层是动态代理
 *      // 2. 开启动态代理 @EnableAspectJAutoProxy(exposeProxy = true) 以后所有的动态代理都是aspect创建的(即使没有接口也可以创建动态代理）
 *      // 3. 使用代理对象来调用方法 本类互调用对象
 *         OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *         orderService.b();
 *         orderService.c();
 *
 *
 */
@EnableAspectJAutoProxy(exposeProxy = true)  // 开启基于注解的aop模式 开启暴露代理对象
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
