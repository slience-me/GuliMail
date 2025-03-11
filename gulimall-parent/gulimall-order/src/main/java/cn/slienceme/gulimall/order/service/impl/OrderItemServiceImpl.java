package cn.slienceme.gulimall.order.service.impl;

import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.order.dao.OrderItemDao;
import cn.slienceme.gulimall.order.entity.OrderItemEntity;
import cn.slienceme.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues：声明需要监听的队列
     * org.springframework.amqp.core.Message
     * 参数可以写以下类型
     * 1. Message：原生消息详细信息 头+体
     * 2. T<发送的消息的类型> OrderReturnReasonEntity content
     * 3. Channel : 当前传输数据的通道
     *
     * @RabbitListener注解只能标注在方法上
     * Queue可以很多人都监听。只要收到消息就删除消息，而且只能有一个人收到消息
     * 场景：
     *  2) 订单服务启动多个，就可以监听同一个队列，一个订单服务处理一个消息，消息处理完成就删除
     *  3) 一个消息处理完，才可以处理下一个
     */
    /*@RabbitListener(queues = {"hello-java-queue"})*/
    @RabbitHandler
    public void recieveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel){
        // System.out.println("收到消息,内容为"+message+"===>类型为"+ content);
        byte[] body = message.getBody();
        MessageProperties messageProperties = message.getMessageProperties();

        // channel内顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 签收货物  非批量模式
        try {
            channel.basicAck(deliveryTag,false);
            System.out.println("签收了货物");
            System.out.println("消息处理完成 === "+content.getId());
        } catch (IOException e) {
            // 网络中断
            throw new RuntimeException(e);
        }

    }

    @RabbitHandler
    public void recieveMessage2(Message message, OrderEntity content, Channel channel){
        // channel内顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 签收货物  非批量模式
        try {
            // boolean requeue 是否重新入队
            channel.basicNack(deliveryTag,false, true);
//            channel.basicAck(deliveryTag,false);
            System.out.println("签收了货物");
            System.out.println("消息处理完成 +++ "+content.getId());
        } catch (IOException e) {
            // 网络中断
            throw new RuntimeException(e);
        }

    }

}
