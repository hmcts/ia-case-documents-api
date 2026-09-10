package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class HearingNoticeTemplateConfigurationTest {

    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private StringProvider stringProvider;

    private HearingNoticeTemplateConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new HearingNoticeTemplateConfiguration(customerServicesProvider);
    }

    @Test
    void should_create_hearing_notice_template() {
        String templateName = "HEARING_NOTICE_TEMPLATE.docx";

        HearingNoticeTemplate template =
            configuration.getHearingNoticeTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }

    @Test
    void should_create_hearing_notice_adjourned_without_date_template() {
        String templateName = "HEARING_NOTICE_ADJOURNED_WITHOUT_DATE_TEMPLATE.docx";

        HearingNoticeTemplate template =
            configuration.getHearingNoticeAdjournedTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }

    @Test
    void should_create_remote_hearing_notice_template() {
        String templateName = "REMOTE_HEARING_NOTICE_TEMPLATE.docx";

        HearingNoticeTemplate template =
            configuration.getRemoteHearingNoticeTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }

    @Test
    void should_create_ada_hearing_notice_template() {
        String templateName = "ADA_HEARING_NOTICE_TEMPLATE.docx";

        HearingNoticeTemplate template =
            configuration.getAdaHearingNoticeTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }

    @Test
    void should_create_notice_of_adjourned_hearing_template() {
        String templateName = "NOTICE_OF_ADJOURNED_HEARING_TEMPLATE.docx";

        HearingNoticeTemplate template =
            configuration.getNoticeOfAdjournedHearingTemplate(templateName, stringProvider);

        assertThat(template).isNotNull();
        assertThat(template.getName()).isEqualTo(templateName);
    }
}
