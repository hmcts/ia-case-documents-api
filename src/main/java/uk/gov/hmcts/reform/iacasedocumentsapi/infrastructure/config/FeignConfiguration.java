package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FeignConfiguration {

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectFactory<HttpMessageConverters> messageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    //    @Bean
    //    public Decoder decoder() {
    //        HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());
    //
    //        return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(jacksonConverter)));
    //    }
    //
    //    public ObjectMapper objectMapper() {
    //        ObjectMapper objectMapper = new ObjectMapper();
    //        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
    //        objectMapper.registerModule(new Jdk8Module());
    //        objectMapper.registerModule(new JavaTimeModule());
    //        return objectMapper;
    //    }
}
