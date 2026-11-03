package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

@Slf4j
@Configuration
@SuppressWarnings("removal")
public class FeignConfiguration {

    @Bean
    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return converters -> {
            log.info("feign converters BEFORE: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
            converters.removeIf(c -> c instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
                    || c instanceof org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter);

            int idx = 0;
            for (int i = 0; i < converters.size(); i++) {
                if (converters.get(i) instanceof ByteArrayHttpMessageConverter
                        || converters.get(i) instanceof StringHttpMessageConverter) {
                    idx = i + 1;
                }
            }
            converters.add(idx, new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
            log.info("feign converters AFTER: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
        };
    }

    @Bean
    public ClientHttpMessageConvertersCustomizer jacksonClientCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return builder -> builder.withJsonConverter(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    @Qualifier("feign")
    public ObjectMapper feignObjectMapper(org.springframework.http.converter.json.Jackson2ObjectMapperBuilder builder) {
        return builder
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .modulesToInstall(
                        new Jdk8Module(),
                        new JavaTimeModule()
                )
                .build();
    }
}
