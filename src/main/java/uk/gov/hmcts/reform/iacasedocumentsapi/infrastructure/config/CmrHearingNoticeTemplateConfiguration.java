package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.CmrHearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Configuration
public class CmrHearingNoticeTemplateConfiguration {

    private final CustomerServicesProvider customerServicesProvider;

    public CmrHearingNoticeTemplateConfiguration(CustomerServicesProvider customerServicesProvider) {
        this.customerServicesProvider = customerServicesProvider;
    }

    @Bean("cmrHearingNoticeTemplate")
    public CmrHearingNoticeTemplate getCmrHearingNoticeTemplate(
        @Value("${cmrHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("cmrRelistedHearingNoticeTemplate")
    public CmrHearingNoticeTemplate getCmrRelistedHearingNoticeTemplate(
            @Value("${cmrRelistedHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteCmrHearingNoticeTemplate")
    public CmrHearingNoticeTemplate getRemoteCmrHearingNoticeTemplate(
            @Value("${remoteCmrHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }
}
