package com.lanhong.chatbot.service.impl;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import com.lanhong.chatbot.dao.IRedisDao;
import com.lanhong.chatbot.dao.IWordDao;
import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.IChat;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;


@Service("openai")
public class ChatGptServiceImpl implements IChat {

    private final Logger logger = LoggerFactory.getLogger(ChatGptServiceImpl.class);
    @Resource
    private OpenAIClient openAIClient;
    @Resource
    private OpenAIAsyncClient openAIAsyncClient;

    @Resource
    private IRedisDao redisDao;

    @Resource
    IWordDao wordDao;

    @Override
    public Mono<String> getChatCompletions(ChatEntity chatEntity) {
        return Mono.fromCallable(() -> {
            List<ChatMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatMessage(ChatRole.USER, chatEntity.getPrompt()));
            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
            ChatCompletions chatCompletions = openAIClient.getChatCompletions(chatEntity.getModel(), chatCompletionsOptions);
            String content = chatCompletions.getChoices().get(0).getMessage().getContent();
            logger.info("chatgpt 回答：" + content);
            return content;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    @Override
    public Flux<String> getChatCompletionsStream(ChatEntity chatEntity) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, chatEntity.getPrompt()));
        return Flux.create(emitter -> {
            openAIAsyncClient.getChatCompletionsStream(chatEntity.getModel(), new ChatCompletionsOptions(chatMessages))
                    .subscribe(chatCompletions -> {
                                for (ChatChoice choice : chatCompletions.getChoices()) {
                                    ChatMessage message = choice.getDelta();
                                    if (message != null && message.getContent() != null) {
                                        // Emit the message content
                                        emitter.next(message.getContent());
                                    }
                                }
                            },
                            error -> {
                                // Emit the error
                                emitter.error(new RuntimeException("There was an error getting chat completions.", error));
                            },
                            () -> {
                                // Complete the Flux
                                emitter.complete();
                            });
        });
    }

    @Async
    public void getChatCompletionStreamWriteToKafka(ChatEntity chatEntity, String userId) {
        Flux<String> completionsStream = getChatCompletionsStream(chatEntity);
        completionsStream.subscribe(element -> {
            wordDao.addWord(userId, element);
        });
    }

    @Async
    public void getChatCompletionStreamWriteToRedis(ChatEntity chatEntity, String userId) {
        Flux<String> completionsStream = getChatCompletionsStream(chatEntity);
        completionsStream.subscribe(element -> {
            redisDao.addStringToSteam("word_" + userId, element);
        });
    }
}
