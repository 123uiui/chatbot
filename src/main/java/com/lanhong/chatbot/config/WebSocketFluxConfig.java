package com.lanhong.chatbot.config;

import com.lanhong.chatbot.handler.*;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;


import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFlux
public class WebSocketFluxConfig {

    @Resource
    private TextWebSocketHandler textWebSocketHandler;
    @Resource
    private TextStreamWebSocketHandler textStreamWebSocketHandler;
//    @Resource
//    private TextKafkaWebSocketHandler textKafkaWebSocketHandler;

    @Resource
    private AudioBase64WebSocketHandler audioBase64WebSocketHandler;

    @Resource
    private AudioBinaryWebSocketHandler audioBinaryWebSocketHandler;

    @Resource
    private AudioBase64KafkaWebSocketHandler audioBase64KafkaWebSocketHandler;


    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/text", textWebSocketHandler);
        map.put("/text-stream", textStreamWebSocketHandler);
//        map.put("/text-kafka", textKafkaWebSocketHandler);
        map.put("/audio-base64", audioBase64WebSocketHandler);
        map.put("/audio-binary", audioBinaryWebSocketHandler);
        map.put("/audio-base64-kafka", audioBase64KafkaWebSocketHandler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1); // before annotated controllers
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
