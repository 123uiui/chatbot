package com.lanhong.chatbot.controller;

import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.IChat;
import com.lanhong.chatbot.service.impl.ChatGptServiceImpl;
import com.lanhong.chatbot.service.impl.RwkvServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final Logger logger = LoggerFactory.getLogger(ChatController.class);
    @Resource(name = "openai")
    private IChat chatGptService;
    @Resource(name = "rwkv")
    private RwkvServiceImpl rwkvService;


    @PostMapping
    public Mono<String> getChatCompletions(@RequestBody ChatEntity chatEntity) {
        return chatGptService.getChatCompletions(chatEntity);
    }


    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getChatCompletionsStream(@RequestBody ChatEntity chatEntity) {
        return chatGptService.getChatCompletionsStream(chatEntity);
    }

}
