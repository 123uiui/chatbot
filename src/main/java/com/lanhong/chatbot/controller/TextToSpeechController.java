package com.lanhong.chatbot.controller;

import com.lanhong.chatbot.pojo.TextEntity;
import com.lanhong.chatbot.service.ITextToSpeech;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tts")
public class TextToSpeechController {
    private final Logger logger = LoggerFactory.getLogger(TextToSpeechController.class);

    @Resource(name = "azure")
    private ITextToSpeech azureTextToSpeech;

    @PostMapping("/azure")
    public ResponseEntity<byte[]> azureTts(@RequestBody TextEntity textObject){
        logger.info("传入的值是："+textObject);
        byte[] audioBinary = azureTextToSpeech.getAudioBinary(textObject.getText());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(audioBinary);
    }



}
