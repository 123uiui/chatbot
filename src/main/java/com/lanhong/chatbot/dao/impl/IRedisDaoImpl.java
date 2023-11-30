package com.lanhong.chatbot.dao.impl;

import com.lanhong.chatbot.dao.IRedisDao;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IRedisDaoImpl implements IRedisDao {

    private final Logger logger = LoggerFactory.getLogger(IRedisDaoImpl.class);
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addStringToSteam(String key, String value) {
        // 创建一个包含数据的Record对象
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                .ofObject(value)
                .withStreamKey(key);
        // 发送到Redis Stream
        RecordId recordId = stringRedisTemplate.opsForStream().add(record);
        logger.info("Record added to stream with ID: " + recordId);

    }

    @Override
    public String readString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void insertString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key,value);
    }

    @Override
    public List<MapRecord<String, String, String>> readLatestStringFromStream(String key, int count) {
        return null;
    }
}
