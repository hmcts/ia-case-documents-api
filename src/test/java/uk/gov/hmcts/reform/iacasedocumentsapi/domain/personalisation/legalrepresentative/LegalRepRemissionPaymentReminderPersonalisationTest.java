package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.REJECTED;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepRemissionPaymentReminderPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    private Long caseId = 12345L;
    private String paymentRejectedReminderTemplateId = "paymentRejectedReminderTemplateId";
    private String paymentPartiallyApprovedReminderTemplateId = "paymentPartiallyApprovedReminderTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String onlineCaseReferenceNumber = "1111222233334444";
    private String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private String appellantGivenNames = "GivenNames";
    private String appellantFamilyName = "FamilyName";
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private String feeAmount = "14000";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String someTestDateEmail = "14/14/2024";
    private LegalRepRemissionPaymentReminderPersonalisation legalRepRemissionPaymentReminderPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class)).thenReturn(Optional.of(someTestDateEmail));


        legalRepRemissionPaymentReminderPersonalisation = new LegalRepRemissionPaymentReminderPersonalisation(
            paymentRejectedReminderTemplateId,
            paymentPartiallyApprovedReminderTemplateId,
            iaExUiFrontendUrl

        );
    }

    @Test
    void should_return_given_template_id_if_partially_approved() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(PARTIALLY_APPROVED));

        assertEquals(paymentPartiallyApprovedReminderTemplateId, legalRepRemissionPaymentReminderPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_template_id_if_rejected() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(REJECTED));

        assertEquals(paymentRejectedReminderTemplateId, legalRepRemissionPaymentReminderPersonalisation.getTemplateId(asylumCase));
    }

    @Test
     void should_return_given_reference_id() {
        assertEquals(caseId + "_REMISSION_REMINDER_DECISION_LEGAL_REPRESENTATIVE",
            legalRepRemissionPaymentReminderPersonalisation.getReferenceId(caseId));
    }

    @Test
     void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        Map<String, String> personalisation =
            legalRepRemissionPaymentReminderPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        assertEquals(someTestDateEmail, personalisation.get("deadline"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals("140.00", personalisation.get("feeAmountRejected"));
    }
}


