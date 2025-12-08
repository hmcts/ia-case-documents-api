package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AiPAppellantRefundRequestedNotificationSmsTest {


    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String refundRequestedAipSmsTemplateId = "refundRequestedAipSmsTemplateId";
    private String refundRequestedAipPaPayLaterSmsTemplateId = "refundRequestedAipPaPayLaterSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppellantMobilePhone = "07123456789";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String refundDateMock = "12/03/2024";
    private int daysToAskReinstate = 14;
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();
    private AiPAppellantRefundRequestedNotificationSms aipAppellantRefundRequestedNotificationSms;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(REQUEST_FEE_REMISSION_DATE, String.class)).thenReturn(Optional.of(refundDateMock));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));

        aipAppellantRefundRequestedNotificationSms = new AiPAppellantRefundRequestedNotificationSms(
            refundRequestedAipSmsTemplateId,
            refundRequestedAipPaPayLaterSmsTemplateId,
            recipientsFinder,
            iaAipFrontendUrl,
            14,
            systemDateProvider
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_SMS",
            aipAppellantRefundRequestedNotificationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> aipAppellantRefundRequestedNotificationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(aipAppellantRefundRequestedNotificationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> aipAppellantRefundRequestedNotificationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        String endAppealDate = LocalDate.now().toString();
        when(asylumCase.read(AsylumCaseDefinition.REQUEST_FEE_REMISSION_DATE, String.class)).thenReturn(Optional.of(endAppealDate));

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationSms.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(systemDateProvider.dueDate(14), personalisation.get("14 days after refund request sent"));

    }

    @Test
    void should_return_personalisation_for_payLater_payOffline() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.PA));

        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("payLater"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("A1234567"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("HO123456"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Test"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("User"));

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationSms.getPersonalisation(asylumCase);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("14 days after remission request sent"));
    }


    @Test
    void should_return_personalisation_for_standard_refund() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationSms.getPersonalisation(callback);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("14 days after refund request sent"));
    }
}
