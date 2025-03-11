package cn.slienceme.gulimall.order;

import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {


    @Autowired
    AmqpAdmin amqpAdmin;


    /**
     * 1. 如何创建Exchange[hello-java.exchange]、Quene、Binding
     * 1）使用AmqpAdmin创建
     * 2. 如何发送消息
     */
    @Test
    public void createExchange() {
        /*
         * String name, 名称
         * boolean durable,  持久化
         * boolean autoDelete, 自动删除
         * Map<String, Object> arguments 参数列表
         * */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueue() {
        /*
         * String name, 名称
         * boolean durable,  持久化
         * boolean exclusive, 是否排他(是否只能本连接使用)
         * boolean autoDelete, 自动删除
         * Map<String, Object> arguments 参数列表
         * */
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBinding() {
        /*
         * Destination destination, 目的地(交换机/队列)
         * DestinationType destinationType, 目的地类型
         * String exchange, 交换机(指定交换机)
         * String routingKey, 路由键
         * Map<String, Object> arguments 参数列表
         * */
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-queue");
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello", "hello world");
        log.info("消息发送成功");
    }

    @Test
    public void sendOrderMessageTest() {
        // 如果发送的消息是一个对象，我们会使用序列化机制，将对象写出去，必须使得对象实现Serializable接口
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setName("测试slience");
        reasonEntity.setCreateTime(new Date());
        // 如想让对象是json格式，需要配置消息转换器
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello", reasonEntity);
        log.info("消息发送成功{}", reasonEntity);
    }

    @Test
    public void sendOrderMessageManyTimesTest() {
        // 如果发送的消息是一个对象，我们会使用序列化机制，将对象写出去，必须使得对象实现Serializable接口
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("测试slience===>" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello", reasonEntity);
                log.info("消息发送成功{}", reasonEntity);
            } else {
                OrderEntity reasonEntity = new OrderEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setReceiverName("测试slience===>" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello", reasonEntity);
                log.info("消息发送成功{}", reasonEntity);
            }

        }
    }
}
