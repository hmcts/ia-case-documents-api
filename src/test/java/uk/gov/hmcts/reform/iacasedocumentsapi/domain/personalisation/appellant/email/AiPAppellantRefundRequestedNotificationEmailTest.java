package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AiPAppellantRefundRequestedNotificationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String refundRequestedAipPaPayLaterEmailTemplateId = "refundRequestedAipPaPayLaterEmailTemplateId";
    private String refundRequestedAipEmailTemplateId = "refundRequestedAipEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantEmail = "fake@faketest.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String refundDateMock = "12/03/2024";
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();
    private AiPAppellantRefundRequestedNotificationEmail aipAppellantRefundRequestedNotificationEmail;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REQUEST_FEE_REMISSION_DATE, String.class)).thenReturn(Optional.of(refundDateMock));

        aipAppellantRefundRequestedNotificationEmail = new AiPAppellantRefundRequestedNotificationEmail(
            refundRequestedAipEmailTemplateId,
            refundRequestedAipPaPayLaterEmailTemplateId,
            iaAipFrontendUrl,
            14,
            recipientsFinder,
            systemDateProvider
        );
    }

    @Test
    void should_return_pa_pay_later_template_id_when_pa_appeal_type_and_pay_later() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        String templateId = aipAppellantRefundRequestedNotificationEmail.getTemplateId(asylumCase);

        assertEquals(refundRequestedAipPaPayLaterEmailTemplateId, templateId);
    }

    @Test
    void should_return_default_template_id_when_pa_appeal_type_and_pay_now() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        String templateId = aipAppellantRefundRequestedNotificationEmail.getTemplateId(asylumCase);

        assertEquals(refundRequestedAipEmailTemplateId, templateId);
    }

    @Test
    void should_return_default_template_id_when_not_pa_appeal_type() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        String templateId = aipAppellantRefundRequestedNotificationEmail.getTemplateId(asylumCase);

        assertEquals(refundRequestedAipEmailTemplateId, templateId);
    }

    @Test
    void should_return_default_template_id_when_appeal_type_is_missing() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        String templateId = aipAppellantRefundRequestedNotificationEmail.getTemplateId(asylumCase);

        assertEquals(refundRequestedAipEmailTemplateId, templateId);
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_EMAIL",
            aipAppellantRefundRequestedNotificationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmail));

        assertTrue(aipAppellantRefundRequestedNotificationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> aipAppellantRefundRequestedNotificationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

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

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationEmail.getPersonalisation(asylumCase);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("14 days after remission request sent"));
    }


    @Test
    void should_return_personalisation_for_standard_refund() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        Map<String, String> personalisation = aipAppellantRefundRequestedNotificationEmail.getPersonalisation(callback);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("14 days after refund request sent"));
    }
}
