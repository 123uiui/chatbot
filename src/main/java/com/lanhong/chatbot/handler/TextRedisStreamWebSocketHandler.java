package com.lanhong.chatbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanhong.chatbot.pojo.ChatEntity;
import com.lanhong.chatbot.service.IChat;
import com.lanhong.chatbot.service.impl.ChatGptServiceImpl;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TextRedisStreamWebSocketHandler implements WebSocketHandler, StreamListener<String, ObjectRecord<String,String>> {
    private final Logger logger = LoggerFactory.getLogger(TextRedisStreamWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> userSinks = new ConcurrentHashMap<>();

    @Resource(name = "openai")
    private IChat chatGptService;

    private final Sinks.Many<String> sink;

    public TextRedisStreamWebSocketHandler() {
        this.sink = Sinks.many().multicast().directBestEffort();
    }

//    @KafkaListener(topics = "t_sentence")
//    public void listen(ConsumerRecord<String, String> record) {
//
//        String producerId = record.key(); // 假设 Kafka 消息的 key 是生产者的 ID
//        String consumerId = producerId.replace("producer", "consumer"); // 将生产者的 ID 转换为消费者的 ID
//        Sinks.Many<String> sink = userSinks.get(consumerId);
//        if (sink != null) {
//            sink.tryEmitNext(record.value());
//        }
//    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        String record = message.getValue();
        logger.info(record);
        String producerId = message.getId().toString(); // 假设 Kafka 消息的 key 是生产者的 ID
        String consumerId = producerId.replace("producer", "consumer"); // 将生产者的 ID 转换为消费者的 ID
        Sinks.Many<String> sink = userSinks.get(consumerId);
        if (sink != null) {
            sink.tryEmitNext(record);
        }
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        //参数1是用户id，如userId=producer_1或者userId=consumer_1
        HttpHeaders headers = session.getHandshakeInfo().getHeaders();
        String userId = headers.getFirst("userId");

        String userType = userId.split("_")[0];
        String userNum = userId.split("_")[1];
        logger.info(String.format("用户id：%s，类型：%s，编号：%s ", userId, userType, userNum));
        sessions.put(userId, session);

        Sinks.Many<String> sink = Sinks.many().multicast().directBestEffort();
        userSinks.put(userId, sink);


        // 1. 打印接收到的消息
        Mono<Void> input = session.receive()
                .doOnNext(message -> {
                    if (message.getType() == WebSocketMessage.Type.TEXT) {
                        // 生产者发送的消息
                        if ("producer".equals(userType)) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            ChatEntity chatEntity = null;
                            try {
                                chatEntity = objectMapper.readValue(message.getPayloadAsText(), ChatEntity.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }

                            logger.info("传入的消息是：" + message.getPayloadAsText());
                            logger.info("异步调用chatgpt写入kafka");
                            chatGptService.getChatCompletionStreamWriteToRedis(chatEntity,userId);
                            //chatGptService.getChatCompletionStreamWriteToKafka(chatEntity, userId);

                        }
                    }
                })
                .then();
        // 2. 从Kafka接收数据并发送给客户端
        Mono<Void> output = sink.asFlux()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(record -> {
                    String consumerId = "consumer" + "_" + userNum;
                    WebSocketSession targetSession = sessions.get(consumerId);
                    if (targetSession != null) {
                        logger.info("session: "+consumerId);
                        logger.info("开始发送："+ record);
                        targetSession.send(Mono.just(targetSession.textMessage(record))).subscribe();
                    }
                })
//                .delayElements(Duration.ofSeconds(2))
//                .map(session::textMessage)
                .then();
        return Mono.zip(input, output).then();
    }


}
