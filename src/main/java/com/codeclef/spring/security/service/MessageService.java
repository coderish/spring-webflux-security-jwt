package com.codeclef.spring.security.service;

import com.codeclef.spring.security.domain.FormattedMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class MessageService {

    public Flux<FormattedMessage> getDefaultMessage(){
        return Flux.just(new FormattedMessage());
    }

    public Flux<FormattedMessage> getCustomMessage(String name){
        return Flux.just(new FormattedMessage(name));
    }
}
