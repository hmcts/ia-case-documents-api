package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        JsonMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(JsonMapper jsonMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf(JacksonJsonHttpMessageConverter.class::isInstance);
        restTemplate.getMessageConverters().add(new JacksonJsonHttpMessageConverter(jsonMapper));
        return restTemplate;
    }
}
