package com.design.messaging.requestreply;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static com.design.messaging.requestreply.Producer.REQUEST_QUEUE;

@Service
public class Consumer {
    private final ObjectMapper objectMapper;
    private final QueueMessagingTemplate messagingTemplate;

    @Autowired
    public Consumer(ObjectMapper objectMapper, QueueMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @SuppressWarnings("unchecked")
    @SqsListener(value = REQUEST_QUEUE)
    public void sqsWorker(String message) throws IOException {
        Map<String, Object> map = objectMapper.readValue(message, Map.class);
        messagingTemplate.send(
                (String) map.get("replyTo"),
                new GenericMessage<>(objectMapper.writeValueAsString(new ReplyMessage((String) map.get("messageId"), "SUCCESS")))
        );
    }

    private class ReplyMessage {
        private String messageId;
        private String reply;

        ReplyMessage(String messageId, String reply) {
            this.messageId = messageId;
            this.reply = reply;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getReply() {
            return reply;
        }
    }
}
