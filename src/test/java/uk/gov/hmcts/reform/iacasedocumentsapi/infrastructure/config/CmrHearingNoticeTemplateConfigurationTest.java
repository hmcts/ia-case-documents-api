package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.CmrHearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
class CmrHearingNoticeTemplateConfigurationTest {

    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingDetailsFinder hearingDetailsFinder;
    @Mock private StringProvider stringProvider;

    private CmrHearingNoticeTemplateConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new CmrHearingNoticeTemplateConfiguration(customerServicesProvider, hearingDetailsFinder);
    }

    @Test
    void should_create_cmr_hearing_notice_template() {
        String templateName = "CMR_HEARING_NOTICE_TEMPLATE.docx";

        CmrHearingNoticeTemplate template =
            configuration.getCmrHearingNoticeTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }

    @Test
    void should_create_remote_cmr_hearing_notice_template() {
        String templateName = "REMOTE_CMR_HEARING_NOTICE_TEMPLATE.docx";

        CmrHearingNoticeTemplate template =
            configuration.getRemoteCmrHearingNoticeTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }
}
