package com.design.messaging.eip;

import com.design.messaging.reply.ReplyMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Publisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private final ReplyMessageDispatcher replyMessageDispatcher;

    @Autowired
    public Producer(ReplyMessageDispatcher replyMessageDispatcher) {
        this.replyMessageDispatcher = replyMessageDispatcher;
    }

    @Scheduled(cron = "*/10 * * * * *")
    @Publisher(channel = RequestReplyFlow.REQUEST_CHANNEL)
    public Message<String> produce() {
        String messageId = "" + System.currentTimeMillis();


        Message<String> message = MessageBuilder.withPayload("MESSAGE")
                .setHeader(MessageHeaders.REPLY_CHANNEL, RequestReplyFlow.REPLY_CHANNEL)
                .setHeader("uid", messageId)
                .build();
        logger.info("Sending {}", messageId);

        replyMessageDispatcher.observable(messageId)
                .timeout(400, TimeUnit.MILLISECONDS)
                .firstElement()
                .subscribe(m -> logger.info("Reply received: {}", m.getPayload()));
        return message;
    }
}
