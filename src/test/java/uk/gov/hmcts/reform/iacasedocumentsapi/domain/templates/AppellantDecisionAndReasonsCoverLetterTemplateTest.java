package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class AppellantDecisionAndReasonsCoverLetterTemplateTest {

    private final String templateName = "";
    private final String someAppealReferenceNumber = "appealReferenceNumber";
    private final String someHomeOfficeReferenceNumber = "homeOfficeRef";
    private final String someGivenNames = "some-given-name";
    private final String someFamilyName = "some-family-name";
    private final AppealDecision appealAllowed = AppealDecision.ALLOWED;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private AppellantDecisionAndReasonsCoverLetterTemplate appellantDecisionAndReasonsCoverLetterTemplate;

    @BeforeEach
    public void setUp() {

        appellantDecisionAndReasonsCoverLetterTemplate = new AppellantDecisionAndReasonsCoverLetterTemplate(
            templateName,
            customerServicesProvider
        );
    }

    @Test
    void returns_correctly_mapped_template_values() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(appealAllowed));
        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateValues = appellantDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(7, templateValues.size());
        assertEquals(templateValues.get("appealReferenceNumber"), someAppealReferenceNumber);
        assertEquals(templateValues.get("homeOfficeReferenceNumber"), someHomeOfficeReferenceNumber);
        assertEquals(templateValues.get("appellantGivenNames"), someGivenNames);
        assertEquals(templateValues.get("appellantFamilyName"), someFamilyName);
        assertEquals("Yes", templateValues.get("allowed"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_template_name() {

        assertEquals(appellantDecisionAndReasonsCoverLetterTemplate.getName(), templateName);
    }

    @Test
    void throws_if_appeal_decision_not_present() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(appealAllowed));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantDecisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appeal decision must be present");
    }
}
