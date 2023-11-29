package com.lanhong.chatbot.config;

import com.lanhong.chatbot.component.SpeechSynthesizerPullOutStream;
import com.lanhong.chatbot.factory.SpeechSynthesizerFactory;
import com.lanhong.chatbot.factory.SpeechSynthesizerPullOutStreamFactory;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpeechSynthesizerConfig {

    private final Logger logger = LoggerFactory.getLogger(SpeechSynthesizerConfig.class);

    @Value("${azure.speech.key}")
    private String subscriptionKey;

    @Value("${azure.speech.region}")
    private String region;

    @Value("${azure.speech.language}")
    private String language;

    @Value("${azure.speech.voiceName}")
    private String voiceName;

    @Bean
    public SpeechConfig speechConfig() {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(subscriptionKey, region);
        speechConfig.setSpeechSynthesisLanguage(language);
        speechConfig.setSpeechSynthesisVoiceName(voiceName);
//        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz48KBitRateMonoMp3);
        return speechConfig;
    }

    @Bean
    public GenericObjectPool<SpeechSynthesizer> synthesizerPool(SpeechConfig speechConfig) {
        //对象连接池设置
        GenericObjectPoolConfig<SpeechSynthesizer> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(2);
        poolConfig.setMaxTotal(64);
        poolConfig.setJmxEnabled(false);
//        poolConfig.setJmxNameBase("poolBase");
//        poolConfig.setJmxNamePrefix("poolPrefix");

        // 创建 SpeechSynthesizer 对象池
        return new GenericObjectPool<>(new SpeechSynthesizerFactory(speechConfig),poolConfig);
    }

    @Bean
    public GenericObjectPool<SpeechSynthesizerPullOutStream> synthesizerPullOutStreamPool(SpeechConfig speechConfig) {
        //对象连接池设置
        GenericObjectPoolConfig<SpeechSynthesizerPullOutStream> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(2);
        poolConfig.setMaxTotal(64);
        poolConfig.setJmxEnabled(false);
//        poolConfig.setJmxNameBase("poolBase");
//        poolConfig.setJmxNamePrefix("poolPrefix");

        // 创建 SpeechSynthesizer 对象池
        return new GenericObjectPool<>(new SpeechSynthesizerPullOutStreamFactory(speechConfig),poolConfig);
    }

}
