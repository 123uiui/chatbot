package com.lanhong.chatbot.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatEntity {
    @JsonProperty("gpt_type")
    private String gptType;
    private String model;
    private String prompt;
    private String ext;
}
