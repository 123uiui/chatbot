package com.lanhong.chatbot.factory;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeechSynthesizerFactory extends BasePooledObjectFactory<SpeechSynthesizer> {
    private final Logger logger = LoggerFactory.getLogger(SpeechSynthesizerFactory.class);

    private SpeechConfig config=null;

    public SpeechSynthesizerFactory(SpeechConfig config) {
        this.config = config;
    }

    @Override
    public SpeechSynthesizer create() {
        logger.info("create a brand new synthesizer");
        return new SpeechSynthesizer(config, null);
    }

    @Override
    public PooledObject<SpeechSynthesizer> wrap(SpeechSynthesizer synthesizer) {
        return new DefaultPooledObject<>(synthesizer);
    }

    @Override
    public void destroyObject(PooledObject<SpeechSynthesizer> p) {
        p.getObject().close();
    }
}