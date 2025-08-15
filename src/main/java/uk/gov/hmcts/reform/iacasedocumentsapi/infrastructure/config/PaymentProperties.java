package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@EnableConfigurationProperties
@ConfigurationProperties("payment.params")
public class PaymentProperties {

    private String organisationUrn;
    private String siteId;

}
