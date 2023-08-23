package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.LoggingInterceptor;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        ObjectMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(
        ObjectMapper objectMapper
    ) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));

        restTemplate
            .getMessageConverters()
            .add(0, mappingJackson2HttpMessageConverter(objectMapper));

        return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        ObjectMapper objectMapper
    ) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
