package com.lanhong.chatbot.service.impl;

import com.lanhong.chatbot.service.ITextToSpeech;
import com.microsoft.cognitiveservices.speech.*;
import jakarta.annotation.Resource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("azure")
public class AzureTextToSpeech implements ITextToSpeech {

    @Resource
    private GenericObjectPool<SpeechSynthesizer> synthesizerPool;

    @Override
    public String getAudioBase64(String text) {
        byte[] audioBinary = getAudioBinary(text);
        return Base64.getEncoder().encodeToString(audioBinary);
    }

    @Override
    public byte[] getAudioBinary(String text) {
        SpeechSynthesizer synthesizer = null;
        try {
            synthesizer = synthesizerPool.borrowObject();
            SpeechSynthesisResult speechSynthesisResult = synthesizer.SpeakTextAsync(text).get();
            if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
                System.out.println("Speech synthesized to speaker for text [" + text + "]");
                return speechSynthesisResult.getAudioData();
            } else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you set the speech resource key and region values?");
                }
            }
            return new byte[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
