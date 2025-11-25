package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiPRemissionRequestAutomaticReminderSmsTest {

    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    AsylumCase asylumCase;
    private Long caseId = 12345L;
    private String paymentRejectedReminderTemplateId = "paymentRejectedReminderTemplateId";
    private String paymentPartiallyApprovedReminderTemplateId = "paymentPartiallyApprovedReminderTemplateId";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String onlineCaseReferenceNumber = "1111222233334444";
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private String iaAipFrontendUrl = "http://localhost";
    private String someTestDateEmail = "14/14/2024";
    private String feeAmount = "14000";
    private AipRemissionRequestAutomaticReminderSms aipRemissionRequestAutomaticReminderSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class)).thenReturn(Optional.of(someTestDateEmail));

        aipRemissionRequestAutomaticReminderSms = new AipRemissionRequestAutomaticReminderSms(
            paymentRejectedReminderTemplateId,
            paymentPartiallyApprovedReminderTemplateId,
            iaAipFrontendUrl,
            recipientsFinder

        );
    }

    @Test
    void should_return_given_template_id_if_partially_approved() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(PARTIALLY_APPROVED));

        assertEquals(paymentPartiallyApprovedReminderTemplateId, aipRemissionRequestAutomaticReminderSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_template_id_if_rejected() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(REJECTED));

        assertEquals(paymentRejectedReminderTemplateId, aipRemissionRequestAutomaticReminderSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_AIP_REMISSION_REMINDER_DECISION_SMS",
            aipRemissionRequestAutomaticReminderSms.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        Map<String, String> personalisation =
            aipRemissionRequestAutomaticReminderSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        assertEquals(someTestDateEmail, personalisation.get("deadline"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals("140.00", personalisation.get("feeAmountRejected"));
    }


}
