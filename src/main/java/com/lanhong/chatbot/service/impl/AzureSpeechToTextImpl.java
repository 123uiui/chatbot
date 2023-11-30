package com.lanhong.chatbot.service.impl;

import com.lanhong.chatbot.pojo.AnyPullAudioInputStreamCallback;
import com.lanhong.chatbot.service.ISpeechToText;
import com.lanhong.chatbot.util.WavStreamUtils;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service("azureStt")
public class AzureSpeechToTextImpl implements ISpeechToText {

    private final Logger logger = LoggerFactory.getLogger(AzureSpeechToTextImpl.class);

    @Resource
    private SpeechConfig speechConfig;

    private StringBuilder result = new StringBuilder();


    @Override
    public String getText(byte[] bytes) {
        InputStream inputStream = new ByteArrayInputStream(bytes);

        AudioStreamFormat audioStreamFormat = AudioStreamFormat.getDefaultInputFormat();
        // WavStreamUtils wavStream = new WavStreamUtils(inputStream);
        AnyPullAudioInputStreamCallback pullAudioInputStream = new AnyPullAudioInputStreamCallback(inputStream);
        PullAudioInputStream pullStream = PullAudioInputStream.createPullStream(pullAudioInputStream, AudioStreamFormat.getCompressedFormat(AudioStreamContainerFormat.ANY));

        AudioConfig audioInput = AudioConfig.fromStreamInput(pullStream);

        String[] languages = {"en-US", "zh-CN"}; // 你想要支持的语言列表
        AutoDetectSourceLanguageConfig autoDetectSourceLanguageConfig = AutoDetectSourceLanguageConfig.fromLanguages(Arrays.asList(languages));

        SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig,autoDetectSourceLanguageConfig, audioInput);
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
