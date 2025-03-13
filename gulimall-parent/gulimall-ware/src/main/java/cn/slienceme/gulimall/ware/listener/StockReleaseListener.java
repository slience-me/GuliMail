package cn.slienceme.gulimall.ware.listener;

import cn.slienceme.common.constant.OrderStatusEnum;
import cn.slienceme.common.to.OrderTo;
import cn.slienceme.common.to.StockDetailTo;
import cn.slienceme.common.to.StockLockedTo;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.ware.constant.WareTaskStatusEnum;
import cn.slienceme.gulimall.ware.entity.WareOrderTaskDetailEntity;
import cn.slienceme.gulimall.ware.entity.WareOrderTaskEntity;
import cn.slienceme.gulimall.ware.feign.OrderFeignService;
import cn.slienceme.gulimall.ware.service.WareOrderTaskDetailService;
import cn.slienceme.gulimall.ware.service.WareOrderTaskService;
import cn.slienceme.gulimall.ware.service.WareSkuService;
import cn.slienceme.gulimall.ware.vo.OrderVo;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
@Service
public class StockReleaseListener {


    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        try {
            wareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleStockLockedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("************************从订单模块收到库存解锁的消息********************************");
        try {
            wareSkuService.unlockOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
