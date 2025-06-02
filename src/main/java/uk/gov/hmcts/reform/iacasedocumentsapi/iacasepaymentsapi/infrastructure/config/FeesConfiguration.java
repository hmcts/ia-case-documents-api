package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@EnableConfigurationProperties
@ConfigurationProperties("fees-register")
public class FeesConfiguration {

    private final Map<String, LookupReferenceData> fees = new HashMap<>();

    @Setter
    @Getter
    public static class LookupReferenceData {
        private String channel;
        private String event;
        private String jurisdiction1;
        private String jurisdiction2;
        private String keyword;
        private String service;

    }
}
