package com.lanhong.chatbot.dao;

import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.List;

public interface IRedisDao {

    void addStringToSteam(String key, String value);

    String readString(String key);

    void insertString(String key, String value);

    List<MapRecord<String, String, String>> readLatestStringFromStream(String key, int count);
}
