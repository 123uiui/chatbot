package com.lanhong.chatbot.service.impl;

import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.IChat;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service("rwkv")
public class RwkvServiceImpl implements IChat {

    @Value("${rwkv.url}")
    private String rwkvUrl;
    @Resource
    private WebClient webClient;

    @Override
    public Mono<String> getChatCompletions(ChatEntity chatEntity) {
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", chatEntity.getPrompt());

        Map<String, Object> data = new HashMap<>();
        data.put("messages", Arrays.asList(systemMessage));
        data.put("temperature", 0.3);
        data.put("max_tokens", 2000);
        data.put("user", "live-virtual-digital-person");


        return webClient.post()
                .uri(rwkvUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer")
                .body(BodyInserters.fromValue(data))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Client Error: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Server Error: " + errorBody))))
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    // handle the exception here
                    System.err.println("Error: " + e.getMessage());
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> getChatCompletionsStream(ChatEntity chatEntity) {
        return null;
    }

    @Override
    public void getChatCompletionStreamWriteToKafka(ChatEntity chatEntity, String userId) {

    }

    @Override
    public void getChatCompletionStreamWriteToRedis(ChatEntity chatEntity, String userId) {

    }
}
