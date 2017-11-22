package com.lawrence.fatalis.config.rabbitmq;

import com.lawrence.fatalis.util.DateUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * rabbitmq配置, direct模式
 */
@Configuration
@ConditionalOnProperty(prefix = "fatalis", name = "rabbitmq-open", havingValue = "true")
public class RabbitConfig {

    /**
     * 队列dateQueue配置
     *
     * @return Queue
     */
    @Bean
    public Queue dateQueue() {

        return new Queue("dateQueue");
    }

}