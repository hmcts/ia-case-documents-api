package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_REASONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BailEndApplicationTemplateTest {

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private CustomerServicesProvider customerServicesProvider;

    private final String govCallChargesUrl = "TEST|https://www.example.com/}";
    private String applicantGivenNames = "John";
    private String applicantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String bailReferenceNumber = "5555-5555-5555-9999";
    private String endApplicationOutcome = "Withdrawn";
    private String endApplicationReasons = "Duplicate case";
    private String endApplicationDate = "2022-05-17";

    private final String templateName = "BAIL_END_APPLICATION_DOCUMENT.docx";

    private BailEndApplicationTemplate bailEndApplicationTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        bailEndApplicationTemplate =
            new BailEndApplicationTemplate(templateName, govCallChargesUrl, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, bailEndApplicationTemplate.getName());
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        dataSetUp();

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        fieldValuesMap = bailEndApplicationTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
    }

    @Test
    void all_data_values_should_be_included_with_no_date_formatted() {
        dataSetUp();
        when(bailCase.read(END_APPLICATION_DATE, String.class)).thenReturn(Optional.empty());

        fieldValuesMap = bailEndApplicationTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("", fieldValuesMap.get("endApplicationDate"));

    }

    //Helper method for common assertions
    private void checkCommonFields() {
        TestCase.assertTrue(fieldValuesMap.containsKey("applicantGivenNames"));
        TestCase.assertTrue(fieldValuesMap.containsKey("applicantFamilyName"));
        TestCase.assertTrue(fieldValuesMap.containsKey("bailReferenceNumber"));
        TestCase.assertTrue(fieldValuesMap.containsKey("homeOfficeReferenceNumber"));
        TestCase.assertTrue(fieldValuesMap.containsKey("endApplicationOutcome"));
        TestCase.assertTrue(fieldValuesMap.containsKey("endApplicationReasons"));
        TestCase.assertTrue(fieldValuesMap.containsKey("endApplicationDate"));
        TestCase.assertTrue(fieldValuesMap.containsKey("customerServicesTelephone"));
        TestCase.assertTrue(fieldValuesMap.containsKey("customerServicesEmail"));
        TestCase.assertTrue(fieldValuesMap.containsKey("govCallChargesLink"));
    }

    // Helper method to set the common data
    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(END_APPLICATION_OUTCOME, String.class)).thenReturn(Optional.of(endApplicationOutcome));
        when(bailCase.read(END_APPLICATION_REASONS, String.class)).thenReturn(Optional.of(endApplicationReasons));
        when(bailCase.read(END_APPLICATION_DATE, String.class)).thenReturn(Optional.of(endApplicationDate));
    }

}