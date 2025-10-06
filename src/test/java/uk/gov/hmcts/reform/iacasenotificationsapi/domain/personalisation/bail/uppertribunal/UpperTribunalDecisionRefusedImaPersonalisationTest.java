package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.uppertribunal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpperTribunalDecisionRefusedImaPersonalisationTest {

    @Mock
    BailCase bailCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final String templateId = "someTemplateId";
    private final String upperTribunalEmailAddress = "upperTribunal@example.com";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String legalRepReference = "someLegalRepReferenceNumber";
    private final String bailReferenceNumber = "someBailReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private String decisionGranted = " Granted";

    private UpperTribunalDecisionRefusedImaPersonalisation upperTribunalDecisionRefusedImaPersonalisation;

    @BeforeEach
    public void setup() {
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(RecordDecisionType.GRANTED));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        upperTribunalDecisionRefusedImaPersonalisation = new UpperTribunalDecisionRefusedImaPersonalisation(
            templateId,
            upperTribunalEmailAddress,
            customerServicesProvider);
    }

    @Test
    public void should_return_given_template_id()  {
        assertEquals(templateId, upperTribunalDecisionRefusedImaPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_RECORD_DECISION_IMA_UPPER_TRIBUNAL",
            upperTribunalDecisionRefusedImaPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(
            upperTribunalDecisionRefusedImaPersonalisation.getRecipientsList(bailCase)
                .contains(upperTribunalEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_with_decision_granted() {
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(RecordDecisionType.GRANTED));

        Map<String, String> personalisation =
            upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation(bailCase);

        assertEquals(" Granted", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_with_decision_Refused() {
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(RecordDecisionType.REFUSED));

        Map<String, String> personalisation =
            upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation(bailCase);

        assertEquals(" Refused", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_with_decision_Refused_Under_Ima() {
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(RecordDecisionType.REFUSED_UNDER_IMA));

        Map<String, String> personalisation =
            upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation(bailCase);

        assertEquals(" Refused under IMA because 28 days have not expired since the date of detention", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(RecordDecisionType.GRANTED));

        Map<String, String> personalisation =
            upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(appellantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(decisionGranted, personalisation.get("decision"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        Map<String, String> personalisation =
            upperTribunalDecisionRefusedImaPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }
}