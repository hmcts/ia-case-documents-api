package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingNoticeUpdatedDetailsRemoteTemplateTest {

    private final String templateName = "HEARING_NOTICE_TEMPLATE.docx";

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;

    private HearingNoticeUpdatedDetailsRemoteTemplate hearingNoticeUpdatedDetailsRemoteTemplate;


    @BeforeEach
    public void setUp() {

        hearingNoticeUpdatedDetailsRemoteTemplate =
            new HearingNoticeUpdatedDetailsRemoteTemplate(
                templateName,
                hearingNoticeUpdatedTemplateProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, hearingNoticeUpdatedDetailsRemoteTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        assertTrue(hearingNoticeUpdatedDetailsRemoteTemplate.mapFieldValues(caseDetails, caseDetailsBefore).isEmpty());
        verify(hearingNoticeUpdatedTemplateProvider, times(1)).mapFieldValues(caseDetails, caseDetailsBefore);
    }
}
