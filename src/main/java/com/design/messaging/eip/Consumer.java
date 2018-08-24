package com.design.messaging.eip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @ServiceActivator(inputChannel = RequestReplyFlow.REQUEST_CHANNEL)
    public Message consume(@Payload String message, @Headers Map<String, String> headers) {
        logger.info("Processing message: {} - {}", headers.get("uid"), message);
        String replyTo = headers.get(MessageHeaders.REPLY_CHANNEL);

        if (replyTo != null) {
            return MessageBuilder
                    .withPayload("REPLY")
                    .setHeader("uid", headers.get("uid"))
                    .build();
        }
        logger.info("Message processed");
        return null;
    }
}
