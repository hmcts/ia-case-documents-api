package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
public class UpdatedTribunalDecisionAndReasonsCoverLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;

    private String templateName = "some-template-name.docx";
    private String someAppealReferenceNumber = "some-appeal-ref";
    private String someHomeOfficeReferenceNumber = "some-home-office-ref";
    private String someLegalRepReferenceNumber = "some-legal-rep-ref";
    private String someGivenNames = "some-given-name";
    private String someFamilyName = "some-family-name";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplate updatedTribunalDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        updatedTribunalDecisionAndReasonsCoverLetterTemplate = new UpdatedTribunalDecisionAndReasonsCoverLetterTemplate(
            templateName,
            customerServicesProvider
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Allowed", "Dismissed"})
    public void returns_correctly_mapped_template_values(String updatedAppealDecisionn) {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someLegalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecisionn));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateValues = updatedTribunalDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(templateValues.size(), 9);
        assertEquals(templateValues.get("hmcts"), "[userImage:hmcts.png]");
        assertEquals(templateValues.get("appealReferenceNumber"), someAppealReferenceNumber);
        assertEquals(templateValues.get("homeOfficeReferenceNumber"), someHomeOfficeReferenceNumber);
        assertEquals(templateValues.get("legalRepReferenceNumber"), someLegalRepReferenceNumber);
        assertEquals(templateValues.get("appellantGivenNames"), someGivenNames);
        assertEquals(templateValues.get("appellantFamilyName"), someFamilyName);
        if (updatedAppealDecisionn.equals("Allowed")) {
            assertEquals(templateValues.get("allowed"), "Yes");
        } else {
            assertEquals(templateValues.get("allowed"), "No");
        }
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_template_name() {

        assertEquals(updatedTribunalDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }

    @Test
    public void throws_if_appeal_decision_not_present() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someLegalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updatedTribunalDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("update appeal decision must be present");
    }
}
