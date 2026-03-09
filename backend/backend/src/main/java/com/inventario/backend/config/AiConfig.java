package com.inventario.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // ChatClient: abstracción de Spring AI para llamar a Gemini
    // Spring AI autoconfigura la conexión usando las propiedades del application.properties
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                Eres un experto analista de inventarios para empresas latinoamericanas.
                Siempre respondes en español.
                Siempre respondes con JSON válido y nada más.
                Nunca incluyes texto fuera del JSON, nunca usas bloques markdown.
                """)
                .build();
    }

    // ObjectMapper: parsea el JSON que devuelve Gemini a objetos Java
    // JavaTimeModule permite parsear fechas como LocalDate/LocalDateTime
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }
}