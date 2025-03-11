package cn.slienceme.gulimall.order.controller;

import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId((long) i);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("测试slience===>" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("消息发送成功{}", reasonEntity);
            } else {
                OrderEntity reasonEntity = new OrderEntity();
                reasonEntity.setId((long) i);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setReceiverName("测试slience===>" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("消息发送成功{}", reasonEntity);
            }

        }
        return "ok";
    }
}
