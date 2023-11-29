package com.lanhong.chatbot.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.HttpClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class ChatGptClientConfig {
    @Value("${openai.key}")
    private String openaiKey;
    @Value("${openai.proxy.host}")
    private String proxyHost;
    @Value("${openai.proxy.port}")
    private int proxyPort;

    @Bean
    public OpenAIClient openAIClient() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(proxyHost,proxyPort));
        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(openaiKey))
                .clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
                .buildClient();
        return client;
    }

    @Bean
    public OpenAIAsyncClient openAIAsyncClient() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(proxyHost,proxyPort));
        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(openaiKey))
                .clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
                .buildAsyncClient();
        return client;
    }





}
