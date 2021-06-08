package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.EndAppealTemplateHelper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EndAppealTemplateTest {

    private final String templateName = "END_APPEAL_NOTICE_TEMPLATE.docx";

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private EndAppealTemplateHelper endAppealTemplateHelper;

    private EndAppealTemplate endAppealNoticeTemplate;

    private final Map<String, Object> fieldValues = new HashMap<>();

    @BeforeEach
    public void setUp() {

        endAppealNoticeTemplate = new EndAppealTemplate(
            templateName, endAppealTemplateHelper
        );
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, endAppealNoticeTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        String legalRepReferenceNumber = "OUR-REF";
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(endAppealTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        Map<String, Object> templateFieldValues = endAppealNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(1, templateFieldValues.size());
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(endAppealTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = endAppealNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(1, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
    }
}
