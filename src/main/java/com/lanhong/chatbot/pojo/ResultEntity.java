package com.lanhong.chatbot.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResultEntity {
    @JsonProperty("audio_base64")
    private String audioBase64;
    private String text;
}
