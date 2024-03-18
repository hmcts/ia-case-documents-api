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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailSubmissionTemplateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailSubmissionUtTemplate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailSubmissionUtTemplateTest {
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailSubmissionTemplateProvider bailSubmissionTemplateProvider;

    private final String templateName = "BAIL_SUBMISSION_TEMPLATE.docx";
    private BailSubmissionUtTemplate bailSubmissionUtTemplate;

    @BeforeEach
    public void setUp() {
        bailSubmissionUtTemplate = new BailSubmissionUtTemplate(templateName, bailSubmissionTemplateProvider);
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, bailSubmissionUtTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        assertTrue(bailSubmissionUtTemplate.mapFieldValues(caseDetails).isEmpty());
        verify(bailSubmissionTemplateProvider, times(1)).mapFieldValues(caseDetails);

    }
}

