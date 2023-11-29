package com.lanhong.chatbot.dao.impl;


import com.lanhong.chatbot.dao.IWordDao;
import jakarta.annotation.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class WordDaoImpl implements IWordDao {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    private String topic = "t_word";
    @Override
    public void addWord(String userId, String text) {
        kafkaTemplate.send(topic,userId, text);
    }
}
