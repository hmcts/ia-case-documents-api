package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Configuration
public class HearingNoticeTemplateConfiguration {

    @Autowired
    private CustomerServicesProvider customerServicesProvider;

    @Bean("hearingNoticeTemplate")
    public HearingNoticeTemplate getHearingNoticeTemplate(
        @Value("${hearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new HearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("hearingNoticeAdjournedWithoutDateTemplate")
    public HearingNoticeTemplate getHearingNoticeAdjournedTemplate(
        @Value("${hearingNoticeAdjournedWithoutDateDocument.templateName}") String templateName,
        StringProvider stringProvider) {
        return new HearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }

    @Bean("remoteHearingNoticeTemplate")
    public HearingNoticeTemplate getRemoteHearingNoticeTemplate(
            @Value("${remoteHearingNoticeDocument.templateName}") String templateName, StringProvider stringProvider) {
        return new HearingNoticeTemplate(templateName, stringProvider, customerServicesProvider);
    }
}
