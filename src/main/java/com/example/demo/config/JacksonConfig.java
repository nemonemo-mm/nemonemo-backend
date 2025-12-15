package com.example.demo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    /**
     * ISO 8601 with Timezone 형식: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     * 예: "2024-01-15T10:30:00.000Z"
     * LocalDateTime을 UTC 기준으로 직렬화
     */
    private static final DateTimeFormatter ISO_8601_WITH_TIMEZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * LocalDateTime을 UTC 기준 ISO 8601 형식으로 직렬화하는 커스텀 Serializer
     */
    private static class LocalDateTimeUtcSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                // LocalDateTime을 UTC로 가정하고 ISO 8601 형식으로 직렬화
                String formatted = value.atOffset(ZoneOffset.UTC).format(ISO_8601_WITH_TIMEZONE);
                gen.writeString(formatted);
            }
        }
    }

    /**
     * ISO 8601 with Timezone 형식 문자열을 LocalDateTime으로 역직렬화하는 커스텀 Deserializer
     */
    private static class LocalDateTimeUtcDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            try {
                // ISO 8601 형식 문자열을 Instant로 파싱한 후 LocalDateTime으로 변환
                Instant instant = Instant.parse(text);
                return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            } catch (Exception e) {
                // 파싱 실패 시 기본 포맷터로 시도
                try {
                    return LocalDateTime.parse(text, ISO_8601_WITH_TIMEZONE);
                } catch (Exception ex) {
                    throw new IOException("Failed to deserialize LocalDateTime: " + text, ex);
                }
            }
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDateTime을 ISO 8601 with Timezone 형식으로 직렬화/역직렬화
        // LocalDateTime은 timezone 정보가 없으므로 UTC로 가정하고 처리
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeUtcSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeUtcDeserializer());
        
        return builder
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

