package com.design.messaging.reply;

import io.reactivex.Observable;
import org.springframework.messaging.Message;

public interface ReplyMessageDispatcher {

    Observable<Message> observable(String uid);
}
