package com.lanhong.chatbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.IChat;
import com.lanhong.chatbot.service.ITextToSpeech;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AudioBase64WebSocketHandler implements WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioBase64WebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> userSinks = new ConcurrentHashMap<>();

    private final Sinks.Many<String> sink;

    @Resource(name = "openai")
    private IChat chatGptService;
    @Resource(name = "azureTts")
    private ITextToSpeech azureTextToSpeech;

    public AudioBase64WebSocketHandler() {

        this.sink = Sinks.many().multicast().directBestEffort();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        //参数1是用户id，如userId=producer_1或者userId=consumer_1
        //String userId = session.getHandshakeInfo().getUri().getQuery();
        HttpHeaders headers = session.getHandshakeInfo().getHeaders();
        String userId = headers.getFirst("userId");

        String userType = userId.split("_")[0];
        String userNum = userId.split("_")[1];
        logger.info(String.format("用户id：%s，类型：%s，编号：%s ", userId, userType, userNum));
        sessions.put(userId, session);

        // Create sink for user
        Sinks.Many<String> userSink = Sinks.many().multicast().directBestEffort();
        userSinks.put(userId, userSink);


        // Handle incoming messages
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> {
                    String consumerId = "consumer" + "_" + userNum;
                    ObjectMapper objectMapper = new ObjectMapper();
                    ChatEntity chatEntity = null;
                    try {
                        chatEntity = objectMapper.readValue(message, ChatEntity.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    Mono<String> chatCompletions = chatGptService.getChatCompletions(chatEntity);
                    chatCompletions.subscribe(value->{
                        String audioBase64 = azureTextToSpeech.getAudioBase64(value);
                        sendMessage(consumerId, audioBase64);
                    });



                })
                .then();
        // Handle outgoing messages
        Mono<Void> output = session.send(userSink.asFlux().map(session::textMessage));
        return Mono.zip(input, output).then();
    }

    public void sendMessage(String userId, String message) {
        Sinks.Many<String> userSink = userSinks.get(userId);
        if (userSink != null) {
            userSink.tryEmitNext(message);
        }
    }
}
