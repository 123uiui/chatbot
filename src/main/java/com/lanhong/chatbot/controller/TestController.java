package com.lanhong.chatbot.controller;

import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.impl.ChatGptServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @Resource
    ChatGptServiceImpl chatGptService;

    @PostMapping("/test")
    public void getChatCompletions(@RequestBody ChatEntity chatEntity) {
        chatGptService.getChatCompletionStreamWriteToKafka(chatEntity,"produer_1");
    }

}
