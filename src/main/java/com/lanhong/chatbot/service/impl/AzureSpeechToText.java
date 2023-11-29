package com.lanhong.chatbot.service.impl;

import com.lanhong.chatbot.service.ISpeechToText;
import com.lanhong.chatbot.util.WavStreamUtils;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import jakarta.annotation.Resource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service("azureStt")
public class AzureSpeechToText implements ISpeechToText {

    private final Logger logger = LoggerFactory.getLogger(AzureSpeechToText.class);

    @Resource
    private SpeechConfig speechConfig;

    private StringBuilder result = new StringBuilder();


    @Override
    public String getText(byte[] bytes) {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        AudioStreamFormat audioStreamFormat = AudioStreamFormat.getDefaultInputFormat();

        WavStreamUtils wavStream = new WavStreamUtils(inputStream);
        PullAudioInputStream pullStream = PullAudioInputStream.createPullStream(wavStream, audioStreamFormat);
        AudioConfig audioInput = AudioConfig.fromStreamInput(pullStream);


        SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig, audioInput);
        //开始识别
        Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();
        // 获取识别的结果
        SpeechRecognitionResult speechRecognitionResult = null;
        try {
            speechRecognitionResult = task.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        // 清空字符串
        result.setLength(0);
        if (speechRecognitionResult.getReason() == ResultReason.RecognizedSpeech) {
            logger.info("RECOGNIZED: Text=" + speechRecognitionResult.getText());
            result.append(speechRecognitionResult.getText());
        } else if (speechRecognitionResult.getReason() == ResultReason.NoMatch) {
            logger.info("NOMATCH: Speech could not be recognized.");
        } else if (speechRecognitionResult.getReason() == ResultReason.Canceled) {
            CancellationDetails cancellation = CancellationDetails.fromResult(speechRecognitionResult);
            logger.info("CANCELED: Reason=" + cancellation.getReason());

            if (cancellation.getReason() == CancellationReason.Error) {
                logger.info("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                logger.info("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                logger.info("CANCELED: Did you set the speech resource key and region values?");
            }
        }

        return result.toString();
    }
}
