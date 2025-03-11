package cn.slienceme.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


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
 * 6. 发送消息
 *
 *
 */
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
