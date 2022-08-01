package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EndAppealAutomaticallyTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String legalRepReferenceNumber = "REF990";
    private String appealReferenceNumber = "HU/11111/2022";
    private String endApplicationDate = "2022-07-28";

    private final String templateName = "END_APPEAL_AUTOMATICALLY_NOTICE_TEMPLATE.docx";

    private EndAppealAutomaticallyTemplate endAppealAutomaticallyTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        endAppealAutomaticallyTemplate =
            new EndAppealAutomaticallyTemplate(templateName);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, endAppealAutomaticallyTemplate.getName());
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        dataSetUp();

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        fieldValuesMap = endAppealAutomaticallyTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
    }

    @Test
    void all_data_values_should_be_included_with_no_date_formatted() {
        dataSetUp();
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.empty());

        fieldValuesMap = endAppealAutomaticallyTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("", fieldValuesMap.get("endAppealDate"));

    }

    //Helper method for common assertions
    private void checkCommonFields() {
        TestCase.assertTrue(fieldValuesMap.containsKey("appealReferenceNumber"));
        TestCase.assertTrue(fieldValuesMap.containsKey("appellantGivenNames"));
        TestCase.assertTrue(fieldValuesMap.containsKey("appellantFamilyName"));
        TestCase.assertTrue(fieldValuesMap.containsKey("homeOfficeReferenceNumber"));
        TestCase.assertTrue(fieldValuesMap.containsKey("legalRepReferenceNumber"));
        TestCase.assertTrue(fieldValuesMap.containsKey("endAppealDate"));
    }

    // Helper method to set the common data
    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endApplicationDate));
    }

}