package com.lanhong.chatbot.component;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;


public class SpeechSynthesizerPullOutStream {

    private SpeechSynthesizer synthesizer;
    private PullAudioOutputStream stream;

    public SpeechSynthesizerPullOutStream(SpeechConfig config) {
        stream = AudioOutputStream.createPullStream();
        AudioConfig audioConfig = AudioConfig.fromStreamOutput(stream);
        synthesizer = new SpeechSynthesizer(config, audioConfig);
    }

    public SpeechSynthesisResult speakTextAsync(String text) throws Exception {
        return synthesizer.SpeakTextAsync(text).get();
    }


    public SpeechSynthesizer getSynthesizer() {
        return this.synthesizer;
    }

    public PullAudioOutputStream getStream() {
        return this.stream;
    }

    public void close() {
        this.stream.close();
        this.synthesizer.close();
    }

}
