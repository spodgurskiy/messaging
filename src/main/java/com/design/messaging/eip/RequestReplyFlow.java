package com.design.messaging.eip;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
@ComponentScan
public class RequestReplyFlow {
    public static final String REQUEST_CHANNEL = "requestChannel";
    public static final String REPLY_CHANNEL = "replyChannel";

    @Bean(REQUEST_CHANNEL)
    public MessageChannel requestChannel() {
        return new DirectChannel();
    }

    @Bean(REPLY_CHANNEL)
    public MessageChannel replyChannel() {
        return new DirectChannel();
    }
}
