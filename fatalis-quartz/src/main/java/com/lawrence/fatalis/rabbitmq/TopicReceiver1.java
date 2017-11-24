package com.lawrence.fatalis.rabbitmq;

import com.lawrence.fatalis.config.rabbitmq.AmqpConfig;
import com.lawrence.fatalis.test.TestObj;
import com.lawrence.fatalis.util.LogUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = AmqpConfig.TOPIC_QUEUE1)
public class TopicReceiver1 {

    @RabbitHandler
    public void receiveMessage(@Payload TestObj message) {

        LogUtil.info(getClass(), "消息队列" + AmqpConfig.TOPIC_QUEUE1 + "接收: " + message.toString());

    }

}
