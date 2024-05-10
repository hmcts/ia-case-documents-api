package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRecordRemissionDecisionPersonalisationSmsTest {

    private Long caseId = 12345L;
    private String aipAppellantRemissionApprovedTemplateId = "aipAppellantRemissionApprovedTemplateId";
    private String aipAppellantRemissionPartiallyApprovedTemplateId = "aipAppellantRemissionPartiallyApprovedTemplateId";
    private String aipAppellantRemissionRejectedTemplateId = "aipAppellantRemissionRejectedTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String appellantMobile = "07781122334";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String onlineCaseReferenceNumber = "1111222233334444";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer@example.com";
    private int daysAfterRemissionDecision = 14;
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private AipAppellantRecordRemissionDecisionPersonalisationSms aipAppellantRecordRemissionDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));

        aipAppellantRecordRemissionDecisionPersonalisationSms = new AipAppellantRecordRemissionDecisionPersonalisationSms(
            aipAppellantRemissionApprovedTemplateId,
            aipAppellantRemissionPartiallyApprovedTemplateId,
            aipAppellantRemissionRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider
        );
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionDecision.class,
        names = {"APPROVED", "PARTIALLY_APPROVED", "REJECTED"})
    void should_return_approved_template_id(RemissionDecision remissionDecision) {
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        switch (remissionDecision) {
            case APPROVED ->
                assertEquals(aipAppellantRemissionApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                assertEquals(aipAppellantRemissionPartiallyApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            case REJECTED ->
                assertEquals(aipAppellantRemissionRejectedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_REMISSION_DECISION_DECIDED_AIP_APPELLANT_SMS",
            aipAppellantRecordRemissionDecisionPersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobile));

        assertTrue(aipAppellantRecordRemissionDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobile));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipAppellantRecordRemissionDecisionPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRecordRemissionDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        assertEquals(amountLeftToPayInGbp, personalisation.get("remainingFee"));
    }
}
