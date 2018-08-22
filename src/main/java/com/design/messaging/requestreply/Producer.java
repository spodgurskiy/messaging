package com.design.messaging.requestreply;

import com.design.messaging.reply.ReplyMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Reply channel per producer
 */
@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    static final String REQUEST_QUEUE = "REQUEST_QUEUE";
    private final QueueMessagingTemplate messagingTemplate;
    private final String workerId;
    private final ReplyMessageDispatcher replyMessageDispatcher;

    @Autowired
    public Producer(QueueMessagingTemplate messagingTemplate, @Value("${messaging.workerId}") String workerId, ReplyMessageDispatcher replyMessageDispatcher) {
        this.messagingTemplate = messagingTemplate;
        this.workerId = workerId;
        this.replyMessageDispatcher = replyMessageDispatcher;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void produceMessage() {
        String messageId = "" + System.currentTimeMillis();

        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageHeaders.REPLY_CHANNEL, "reply_" + workerId);
        headers.put("uid", messageId);
        GenericMessage<String> message = new GenericMessage<>("MESSAGE", headers);
        logger.info("Sending {}", messageId);
        messagingTemplate.send(REQUEST_QUEUE, message);

        Message reply = replyMessageDispatcher.observable(messageId)
                .timeout(400, TimeUnit.MILLISECONDS)
                .firstElement()
                .blockingGet();

        logger.info("Reply received: {}", reply.getPayload());
    }
}
