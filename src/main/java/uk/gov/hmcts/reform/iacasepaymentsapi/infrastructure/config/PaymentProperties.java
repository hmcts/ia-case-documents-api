package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("payment.params")
public class PaymentProperties {

    private String organisationUrn;
    private String siteId;

    public String getOrganisationUrn() {
        return organisationUrn;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setOrganisationUrn(String organisationUrn) {
        this.organisationUrn = organisationUrn;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
