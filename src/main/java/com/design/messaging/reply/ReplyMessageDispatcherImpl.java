package com.design.messaging.reply;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

/**
 * Reply Mediator
 */
@Service
public class ReplyMessageDispatcherImpl implements ReplyMessageDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ReplyMessageDispatcherImpl.class);
    private final AmazonSQSAsync amazonSQSAsync;
    private final String workerId;
    private final PublishSubject<Message> bus = PublishSubject.create();

    @Autowired
    public ReplyMessageDispatcherImpl(AmazonSQSAsync amazonSQSAsync, @Value("${messaging.workerId}") String workerId) {
        this.amazonSQSAsync = amazonSQSAsync;
        this.workerId = workerId;
    }

    @SqsListener("reply_${messaging.workerId}")
    public void replyListener(@Headers Map<String, Object> headers, @Payload String message) {
        String uid = (String) headers.get("uid");
        logger.info("Received reply {}", uid);
        if (uid == null) {
            logger.warn("Ignoring message with NULL uid");
        } else {
            if (bus.hasObservers()) {
                bus.onNext(new GenericMessage<>(message, headers));
            }
        }
    }

    @Override
    public Observable<Message> observable(String uid) {
        return this.bus
                .filter(Objects::nonNull)
                .filter(o -> Objects.equals(o.getHeaders().get("uid"), uid));
    }


    @PostConstruct
    public void init() {
        amazonSQSAsync.createQueue("reply_" + workerId);
    }
}
