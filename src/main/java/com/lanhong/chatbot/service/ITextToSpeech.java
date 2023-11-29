package com.lanhong.chatbot.service;

public interface ITextToSpeech {
    String getAudioBase64(String text);

    byte[] getAudioBinary(String text);
}
