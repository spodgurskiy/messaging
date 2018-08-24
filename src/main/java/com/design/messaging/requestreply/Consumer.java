package com.design.messaging.requestreply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.design.messaging.requestreply.Producer.REQUEST_QUEUE;

@Service
public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private final QueueMessagingTemplate messagingTemplate;

    @Autowired
    public Consumer(QueueMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @SqsListener(value = REQUEST_QUEUE)
    public void sqsWorker(@Headers Map<String, String> headers) {
        logger.info("Processing message: {}", headers.get("uid"));
        String replyTo = headers.get(MessageHeaders.REPLY_CHANNEL);

        if (replyTo == null) {
            logger.info("Message processed");
        } else {
            Message<String> message = MessageBuilder
                    .withPayload("REPLY")
                    .setHeader("uid", headers.get("uid"))
                    .build();
            messagingTemplate.send(replyTo, message);
        }
    }
}
