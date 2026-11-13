package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public JsonMapper jacksonObjectMapper() {
        return JsonMapperFactory.CUSTOM_MAPPER;
    }
}
