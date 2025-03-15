package cn.slienceme.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class MyRabbitConfig {

    RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }


    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1. 服务器收到消息就回调
     *      1). spring.rabbitmq.publisher-confirms=true
     *      2). 设置确认回调
     * 2. 消息正确抵达队列回调
     *      1). spring.rabbitmq.publisher-returns=true
     *      2). spring.rabbitmq.template.mandatory=true
     *      3). 设置确认回调
     * 3. 消息消费端确认(保证每个消息都被正确消费, 消费完再移除)
     *      1). 默认是自动确认, 我们改为手动确认
     *      2). spring.rabbitmq.listener.simple.acknowledge-mode=manual
     *      3). 如果消息消费失败, 让他重新入队
     *      4). 手动确认代码演示
     */
    //@PostConstruct //  myRabbitConfig对象创建完成后执行这个方法
    public void initRabbitTemplate() {
        // 设置回调函数
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *  当消息成功投递到交换机的时候会触发这个回调(只要消息抵达Broker就ack=true)
             * @param correlationData 当前消息的唯一关联数据（这个是自定义的）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("exec...correlationData={" + correlationData + "}==>ack={" + ack + "}==>cause={" + cause + "ack");
            }
        });
        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *  消息投递到交换机了，但是没有投递到队列
             * @param message 投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange 交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("Message{" + message + "}==>replyCode{" + replyCode + "}==>replyText{" + replyText + "}==>exchange{" + exchange + "}==>routingKey{" + routingKey + "}");
            }
        });
    }


}
