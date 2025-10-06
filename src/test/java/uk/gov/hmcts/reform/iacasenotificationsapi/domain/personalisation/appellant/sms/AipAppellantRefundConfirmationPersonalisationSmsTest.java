package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRefundConfirmationPersonalisationSmsTest {

    private Long caseId = 12345L;
    private String refundConfirmationTemplateId = "refundConfirmationTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "appealReferenceNumber";
    private int daysAfterRemissionDecision = 14;
    private String newFeeAmount = "8000";
    private String withHearing = "decisionWithHearing";
    private String withoutHearing = "decisionWithoutHearing";
    private String appellantMobileNumber = "07781122334";

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AipAppellantRefundConfirmationPersonalisationSms aipAppellantRefundConfirmationPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));

        aipAppellantRefundConfirmationPersonalisationSms = new AipAppellantRefundConfirmationPersonalisationSms(
            refundConfirmationTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider
        );
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(aipAppellantRefundConfirmationPersonalisationSms.getTemplateId(asylumCase).contains(refundConfirmationTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_SMS",
            aipAppellantRefundConfirmationPersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        Mockito.when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        assertTrue(aipAppellantRefundConfirmationPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobileNumber));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipAppellantRefundConfirmationPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRefundConfirmationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("Decision with hearing", personalisation.get("previousDecisionHearingFeeOption"));
        assertEquals("Decision without hearing", personalisation.get("updatedDecisionHearingFeeOption"));
        assertEquals("80.00", personalisation.get("newFee"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("dueDate"));
    }

}
