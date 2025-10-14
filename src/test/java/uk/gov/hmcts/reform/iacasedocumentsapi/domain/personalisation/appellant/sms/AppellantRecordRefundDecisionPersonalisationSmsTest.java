package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantRecordRefundDecisionPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantRecordRefundDecisionPersonalisationSmsTest {

    private Long caseId = 12345L;
    private String appellantRefundApprovedTemplateId = "appellantRefundApprovedTemplateId";
    private String appellantRefundPartiallyApprovedTemplateId = "appellantRefundPartiallyApprovedTemplateId";
    private String appellantRefundRejectedTemplateId = "appellantRefundRejectedTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String appellantMobile = "07781122334";
    private String appealReferenceNumber = "appealReferenceNumber";
    private int daysAfterRefundDecision = 14;
    private String amountRemitted = "4000";


    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private AppellantRecordRefundDecisionPersonalisationSms appellantRecordRefundDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));

        appellantRecordRefundDecisionPersonalisationSms = new AppellantRecordRefundDecisionPersonalisationSms(
            appellantRefundApprovedTemplateId,
            appellantRefundPartiallyApprovedTemplateId,
            appellantRefundRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRefundDecision,
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
                    assertEquals(appellantRefundApprovedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                    assertEquals(appellantRefundPartiallyApprovedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            case REJECTED ->
                    assertEquals(appellantRefundRejectedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REFUND_DECISION_DECIDED_AIP_APPELLANT_SMS",
                appellantRecordRefundDecisionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobile));

        assertTrue(appellantRecordRefundDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobile));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> appellantRecordRefundDecisionPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.APPROVED));

        final String dueDate = LocalDate.now().plusDays(daysAfterRefundDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRefundDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
                appellantRecordRefundDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(systemDateProvider.dueDate(daysAfterRefundDecision), personalisation.get("14DaysAfterRefundDecision"));
        assertEquals("40.00", personalisation.get("refundAmount"));
    }
}
