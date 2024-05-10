package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper;

@ExtendWith(MockitoExtension.class)
class UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplateTest {

    private final String templateName = "";
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;
    private UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate updatedTribunalAipDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        updatedTribunalAipDecisionAndReasonsCoverLetterTemplate = new UpdatedTribunalAipDecisionAndReasonsCoverLetterTemplate(
            templateName,
            templateHelper
        );
    }

    @Test
    void returns_correctly_mapped_template_values() {

        updatedTribunalAipDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);
        verify(templateHelper, times(1)).getCommonMapFieldValues(caseDetails);
    }

    @Test
    void should_return_template_name() {
        assertEquals(updatedTribunalAipDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }

}
