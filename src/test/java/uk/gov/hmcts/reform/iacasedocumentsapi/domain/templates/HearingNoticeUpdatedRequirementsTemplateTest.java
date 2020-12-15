package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HearingNoticeUpdatedRequirementsTemplateTest {

    private final String templateName = "HEARING_NOTICE_TEMPLATE.docx";

    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;

    private HearingNoticeUpdatedRequirementsTemplate hearingNoticeUpdatedRequirementsTemplate;


    @BeforeEach
    public void setUp() {

        hearingNoticeUpdatedRequirementsTemplate =
            new HearingNoticeUpdatedRequirementsTemplate(
                templateName,
                stringProvider,
                hearingNoticeUpdatedTemplateProvider
            );
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, hearingNoticeUpdatedRequirementsTemplate.getName());
        assertNotEquals("", hearingNoticeUpdatedRequirementsTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        assertTrue(hearingNoticeUpdatedRequirementsTemplate.mapFieldValues(caseDetails, caseDetailsBefore).isEmpty());
        verify(hearingNoticeUpdatedTemplateProvider, times(1)).mapFieldValues(caseDetails, caseDetailsBefore);

    }
}
