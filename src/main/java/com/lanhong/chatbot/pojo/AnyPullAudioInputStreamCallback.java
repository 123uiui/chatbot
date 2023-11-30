package com.lanhong.chatbot.pojo;

import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;

import java.io.InputStream;

public class AnyPullAudioInputStreamCallback extends PullAudioInputStreamCallback {
    private InputStream inputStream;

    public AnyPullAudioInputStreamCallback(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read(byte[] dataBuffer) {
        try {
            return inputStream.read(dataBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

