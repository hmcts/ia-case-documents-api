package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.CmrHearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.CmrRelistedHearingNoticeTemplate;
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
    public CmrRelistedHearingNoticeTemplate getCmrRelistedHearingNoticeTemplate(
            @Value("${cmrRelistedHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrRelistedHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteCmrHearingNoticeTemplate")
    public CmrHearingNoticeTemplate getRemoteCmrHearingNoticeTemplate(
            @Value("${remoteCmrHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteCmrRelistedHearingNoticeTemplate")
    public CmrRelistedHearingNoticeTemplate getRemoteCmrRelistedHearingNoticeTemplate(
            @Value("${remoteCmrRelistedHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrRelistedHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteCmrLrHearingNoticeTemplate")
    public CmrHearingNoticeTemplate getRemoteCmrLrHearingNoticeTemplate(
            @Value("${remoteCmrLrHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteCmrLrRelistedHearingNoticeTemplate")
    public CmrRelistedHearingNoticeTemplate getRemoteCmrLrRelistedHearingNoticeTemplate(
            @Value("${remoteCmrLrRelistedHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new CmrRelistedHearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }
}
