package com.lanhong.chatbot.factory;


import com.lanhong.chatbot.component.SpeechSynthesizerPullOutStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeechSynthesizerPullOutStreamFactory extends BasePooledObjectFactory<SpeechSynthesizerPullOutStream> {

    private final Logger logger = LoggerFactory.getLogger(SpeechSynthesizerPullOutStreamFactory.class);
    private SpeechConfig config = null;
    private AudioConfig audioConfig = null;
    public SpeechSynthesizerPullOutStreamFactory(SpeechConfig config, AudioConfig audioConfig) {
        this.config = config;
        this.audioConfig = audioConfig;
    }

    public SpeechSynthesizerPullOutStreamFactory(SpeechConfig config) {
        this(config, null);
    }


    @Override
    public SpeechSynthesizerPullOutStream create() throws Exception {
        logger.info("create a brand new synthesizerPullOutStream");

        return new SpeechSynthesizerPullOutStream(config);
    }

    @Override
    public PooledObject<SpeechSynthesizerPullOutStream> wrap(SpeechSynthesizerPullOutStream speechSynthesizerPullOutStream) {
       return new DefaultPooledObject<>(speechSynthesizerPullOutStream);
    }

    @Override
    public void destroyObject(PooledObject<SpeechSynthesizerPullOutStream> p, DestroyMode destroyMode) throws Exception {
        p.getObject().getSynthesizer().close();
    }
}
