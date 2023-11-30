package com.lanhong.chatbot.service.impl;

import com.lanhong.chatbot.service.ITextToSpeech;
import com.microsoft.cognitiveservices.speech.*;
import jakarta.annotation.Resource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("azureTts")
public class AzureTextToSpeechImpl implements ITextToSpeech {
    private final Logger logger = LoggerFactory.getLogger(AzureTextToSpeechImpl.class);

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
                logger.info("Speech synthesized to speaker for text [" + text + "]");
                return speechSynthesisResult.getAudioData();
            } else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
                logger.info("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    logger.info("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    logger.info("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    logger.info("CANCELED: Did you set the speech resource key and region values?");
                }
            }
            return new byte[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
