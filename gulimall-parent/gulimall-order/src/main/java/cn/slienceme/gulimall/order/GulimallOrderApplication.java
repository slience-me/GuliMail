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
 * Seata控制分布式事务
 * 1) 每一个微服务先必须创建 undo_log 表
 * 2) 安装事务协调器：seata-server：<a href="https://github.com/apache/incubator-seata/releases">seata-server</a>
 * 3) 整合(这个部分我选择docker版本放到服务器了)
 *      3.1 导入依赖 spring-cloud-starter-alibaba-seata 然后确认seata:all版本 当前是 0.7.1
 *      3.2 解压并启动seata-server：
 *          registry-conf: 注册中心相关； 修改 registry type=nacos
 *          config: 配置中心相关； 修改 config type=nacos  或者直接使用file.conf
 *      3.3 所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 *      3.4 每个微服务，都必须导入
 *          registry.conf
 *          file.conf -> vgroup_mapping.{gulimall-ware改为应用名称}-fescar-service-group = "default"
 *      3.5 启动测试分布式事务
 *      3.6 给分布式大事务的入口标注@GlobalTransactional
 *      3.7 每一个远程的小事务用@Transactional
 *
 */
// @EnableAspectJAutoProxy(exposeProxy = true)  // 开启基于注解的aop模式 开启暴露代理对象
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit // 开启rabbitmq
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
