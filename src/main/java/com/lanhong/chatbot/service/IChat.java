package com.lanhong.chatbot.service;

import com.lanhong.chatbot.pojo.ChatEntity;
import org.springframework.scheduling.annotation.Async;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IChat {
    Mono<String> getChatCompletions(ChatEntity chatEntity);

    Flux<String> getChatCompletionsStream(ChatEntity chatEntity);

    void getChatCompletionStreamWriteToKafka(ChatEntity chatEntity,String userId);

    void getChatCompletionStreamWriteToRedis(ChatEntity chatEntity,String userId);
}
