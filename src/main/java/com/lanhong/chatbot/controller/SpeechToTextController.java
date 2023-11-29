package com.lanhong.chatbot.controller;


import com.lanhong.chatbot.service.ISpeechToText;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/stt")
public class SpeechToTextController {

    @Resource(name = "azureStt")
    private ISpeechToText azureSpeechToText;

    @PostMapping("/azure/file")
    public String getText(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String text = azureSpeechToText.getText(bytes);
        return text;
    }

    @PostMapping("/azure/audio")
    public String getText(@RequestBody byte[] audioData) {
        String text = azureSpeechToText.getText(audioData);
        return text;
    }

}
