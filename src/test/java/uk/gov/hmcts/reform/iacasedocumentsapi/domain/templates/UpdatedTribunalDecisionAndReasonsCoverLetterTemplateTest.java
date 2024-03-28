package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper;

@ExtendWith(MockitoExtension.class)
public class UpdatedTribunalDecisionAndReasonsCoverLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;

    private String templateName = "some-template-name.docx";
    private String someLegalRepReferenceNumber = "some-legal-rep-ref";

    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplate updatedTribunalDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        updatedTribunalDecisionAndReasonsCoverLetterTemplate = new UpdatedTribunalDecisionAndReasonsCoverLetterTemplate(
            templateName,
            templateHelper
        );
    }

    @Test
    public void returns_correctly_mapped_template_values() {

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someLegalRepReferenceNumber));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateValues = updatedTribunalDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        verify(templateHelper, times(1)).getCommonMapFieldValues(caseDetails);
        assertEquals(templateValues.get("legalRepReferenceNumber"), someLegalRepReferenceNumber);
    }

    @Test
    public void should_return_template_name() {

        assertEquals(updatedTribunalDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }
}
