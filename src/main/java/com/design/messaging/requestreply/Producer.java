package com.design.messaging.requestreply;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reply channel per producer
 */
@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    static final String REQUEST_QUEUE = "REQUEST_QUEUE";
    private final AmazonSQSAsync amazonSQSAsync;
    private final QueueMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private Map<String, java.util.function.Consumer<Map>> requests = Collections.synchronizedMap(new HashMap<>());
    private String hostname;

    @Autowired
    public Producer(AmazonSQSAsync amazonSQSAsync, QueueMessagingTemplate messagingTemplate, ObjectMapper objectMapper, @Value("${messaging.hostname}") String hostname) {
        this.amazonSQSAsync = amazonSQSAsync;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.hostname = hostname;
    }

    @Scheduled(cron = "* * * * * *")
    public void produceMessage() throws JsonProcessingException {
        String messageId = "" + System.currentTimeMillis();
        requests.put(messageId, this::replyConsumer);
        messagingTemplate.send(REQUEST_QUEUE, new GenericMessage<>(objectMapper.writeValueAsString(
                new Message(messageId, "reply_" + hostname)))
        );
    }

    @SuppressWarnings("unchecked")
    @SqsListener("reply_${messaging.hostname}")
    public void replyListener(String message) throws IOException {
        Map<String, Object> data = objectMapper.readValue(message, Map.class);
        String id = (String) data.get("messageId");
        if (!requests.containsKey(id)) {
            logger.warn("Received unknown reply {}", data);
        } else {
            requests.get(id).accept(data);
            requests.remove(id);
        }
    }

    private void replyConsumer(Map data) {
        logger.info("Received reply {}", data);
    }

    @PostConstruct
    public void postConstruct() {
        amazonSQSAsync.createQueue("reply_" + hostname);
    }

    private class Message {
        private String messageId;
        private String replyTo;

        Message(String messageId, String replyTo) {
            this.messageId = messageId;
            this.replyTo = replyTo;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getReplyTo() {
            return replyTo;
        }
    }
}
