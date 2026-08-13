package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOVAL_OF_24W_APPLICATION_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_OF_24W_DECISION_JUDGE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_OF_24W_DECISION_REASON;

@ExtendWith(MockitoExtension.class)
class Stf24WeeksRemovalDecisionTemplateTest {

    private final String templateName = "APPEAL_REASONS_TEMPLATE.docx";
    private final String appealReferenceNumber = "RP/11111/2020";
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private Stf24WeeksRemovalDecisionTemplate stf24WeekTemplate;

    @BeforeEach
    public void setUp() {

        stf24WeekTemplate = new Stf24WeeksRemovalDecisionTemplate(templateName);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, stf24WeekTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String appellantGivenNames = "Talha";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "Awan";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String homeOfficeReferenceNumber = "A1234567/001";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        String legalRepReferenceNumber = "some-ref";
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        String removalReason = "This is the reason for the removal of 24 week decision";
        when(asylumCase.read(REMOVAL_OF_24W_DECISION_REASON, String.class)).thenReturn(Optional.of(removalReason));
        String judgeName = "some name";
        when(asylumCase.read(REMOVAL_OF_24W_DECISION_JUDGE, String.class)).thenReturn(Optional.of(judgeName));

        Map<String, Object> templateFieldValues = stf24WeekTemplate.mapFieldValues(caseDetails);
        assertEquals(11, templateFieldValues.size());

        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames + " " + appellantFamilyName,
            templateFieldValues.get("appellantFullName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("Legal representative reference", templateFieldValues.get("legalRepRefTitle"));
        assertEquals("suitable", templateFieldValues.get("suitability"));
        assertEquals("Therefore this appeal will continue to be processed under the "
            + "accelerated statutory 24 week timeline.", templateFieldValues.get("postAmble"));
        assertEquals(removalReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(judgeName, templateFieldValues.get("judgeName"));
        assertNotNull(templateFieldValues.get("decisionDate"));
    }

    @Test
    void should_map_case_data_to_template_field_values_without_lr_or_refusal() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String appellantGivenNames = "Talha";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "Awan";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String homeOfficeReferenceNumber = "A1234567/001";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        String removalReason = "This is the reason for the removal of 24 week decision";
        when(asylumCase.read(REMOVAL_OF_24W_DECISION_REASON, String.class)).thenReturn(Optional.of(removalReason));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());
        String judgeName = "some name";
        when(asylumCase.read(REMOVAL_OF_24W_DECISION_JUDGE, String.class)).thenReturn(Optional.of(judgeName));

        Map<String, Object> templateFieldValues = stf24WeekTemplate.mapFieldValues(caseDetails);
        assertEquals(9, templateFieldValues.size());

        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames + " " + appellantFamilyName,
            templateFieldValues.get("appellantFullName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertNull(templateFieldValues.get("legalRepReferenceNumber"));
        assertNull(templateFieldValues.get("legalRepRefTitle"));
        assertEquals("unsuitable", templateFieldValues.get("suitability"));
        assertEquals("Therefore this appeal will no longer be processed under the accelerated "
                + "statutory 24 week timeline and will be processed under the standard appeal process.",
            templateFieldValues.get("postAmble"));
        assertEquals(removalReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(judgeName, templateFieldValues.get("judgeName"));
        assertNotNull(templateFieldValues.get("decisionDate"));
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateFieldValues = stf24WeekTemplate.mapFieldValues(caseDetails);
        assertEquals(9, templateFieldValues.size());

        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantFullName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertNull(templateFieldValues.get("legalRepReferenceNumber"));
        assertNull(templateFieldValues.get("legalRepRefTitle"));
        assertEquals("unsuitable", templateFieldValues.get("suitability"));
        assertEquals("Therefore this appeal will no longer be processed under the accelerated "
                + "statutory 24 week timeline and will be processed under the standard appeal process.",
            templateFieldValues.get("postAmble"));
        assertEquals("", templateFieldValues.get("suitabilityReason"));
        assertEquals("", templateFieldValues.get("judgeName"));
        assertNotNull(templateFieldValues.get("decisionDate"));
    }
}