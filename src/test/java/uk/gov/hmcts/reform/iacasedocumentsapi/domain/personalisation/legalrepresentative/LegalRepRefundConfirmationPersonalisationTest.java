package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_DECISION_HEARING_FEE_OPTION;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepRefundConfirmationPersonalisationTest {
    private Long caseId = 12345L;
    private String refundConfirmationTemplateId = "refundConfirmationTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String legalRepReferenceNumber = "legalRepReferenceNumber";
    private String appellantGivenNames = "GivenNames";
    private String appellantFamilyName = "FamilyName";
    private int daysAfterRemissionDecision = 14;
    private String newFeeAmount = "8000";
    private String withHearing = "decisionWithHearing";
    private String withoutHearing = "decisionWithoutHearing";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private LegalRepRefundConfirmationPersonalisation legalRepRefundConfirmationPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepRefundConfirmationPersonalisation = new LegalRepRefundConfirmationPersonalisation(
            refundConfirmationTemplateId,
            iaExUiFrontendUrl,
            systemDateProvider,
            daysAfterRemissionDecision,
            customerServicesProvider
        );
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(legalRepRefundConfirmationPersonalisation.getTemplateId(asylumCase).contains(refundConfirmationTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_REFUND_CONFIRMATION_LEGAL_REPRESENTATIVE",
            legalRepRefundConfirmationPersonalisation.getReferenceId(12345L));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> legalRepRefundConfirmationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            legalRepRefundConfirmationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("Decision with hearing", personalisation.get("previousDecisionHearingFeeOption"));
        assertEquals("Decision without hearing", personalisation.get("updatedDecisionHearingFeeOption"));
        assertEquals("80.00", personalisation.get("newFee"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
