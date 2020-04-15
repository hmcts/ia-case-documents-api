package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@RunWith(MockitoJUnitRunner.class)
public class DecisionAndReasonsCoverLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;

    private String templateName = "some-template-name.docx";
    private String someAppealReferenceNumber = "some-appeal-ref";
    private String someHomeOfficeReferenceNumber = "some-home-office-ref";
    private String someLegalRepReferenceNumber = "some-legal-rep-ref";
    private String someGivenNames = "some-given-name";
    private String someFamilyName = "some-family-name";
    private AppealDecision appealAllowed = AppealDecision.ALLOWED;

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private DecisionAndReasonsCoverLetterTemplate decisionAndReasonsCoverLetterTemplate;

    @Before
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someLegalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(appealAllowed));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        decisionAndReasonsCoverLetterTemplate = new DecisionAndReasonsCoverLetterTemplate(
            templateName,
            customerServicesProvider
        );
    }

    @Test
    public void returns_correctly_mapped_template_values() {

        Map<String, Object> templateValues = decisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails);

        assertThat(templateValues.size()).isEqualTo(9);
        assertThat(templateValues.get("hmcts")).isEqualTo("[userImage:hmcts.png]");
        assertThat(templateValues.get("appealReferenceNumber")).isEqualTo(someAppealReferenceNumber);
        assertThat(templateValues.get("homeOfficeReferenceNumber")).isEqualTo(someHomeOfficeReferenceNumber);
        assertThat(templateValues.get("legalRepReferenceNumber")).isEqualTo(someLegalRepReferenceNumber);
        assertThat(templateValues.get("appellantGivenNames")).isEqualTo(someGivenNames);
        assertThat(templateValues.get("appellantFamilyName")).isEqualTo(someFamilyName);
        assertThat(templateValues.get("allowed")).isEqualTo("Yes");
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_template_name() {

        assertThat(decisionAndReasonsCoverLetterTemplate.getName())
            .isEqualTo(templateName);
    }

    @Test
    public void throws_if_appeal_decision_not_present() {

        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsCoverLetterTemplate.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appeal decision must be present");
    }
}
