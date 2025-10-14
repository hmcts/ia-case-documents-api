package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MANAGE_FEE_REQUESTED_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_FEE_AMOUNT_GBP;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AipAppellantManageFeeUpdatePersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantManageFeeUpdatePersonalisationSmsTest {
    private String aipAppellantManageFeeUpdateSmsTemplateId = "aipAppellantManageFeeUpdateSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppellantMobilePhone = "07123456789";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String onlineCaseReferenceNumber = "1111222233334444";
    private String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private int daysAfterRemissionDecision = 14;
    private String feeAmount = "4000";
    private String newFeeAmount = "2000";
    private String manageFeeRequestedAmount = "2000";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    FeatureToggler featureToggler;

    private AipAppellantManageFeeUpdatePersonalisationSms aipAppellantManageFeeUpdatePersonalisationSms;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.of(manageFeeRequestedAmount));

        aipAppellantManageFeeUpdatePersonalisationSms = new AipAppellantManageFeeUpdatePersonalisationSms(
            aipAppellantManageFeeUpdateSmsTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider,
            featureToggler
        );
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(aipAppellantManageFeeUpdatePersonalisationSms.getTemplateId(asylumCase).contains(aipAppellantManageFeeUpdateSmsTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_MANAGE_FEE_UPDATE_AIP_APPELLANT_SMS",
            aipAppellantManageFeeUpdatePersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);

        assertTrue(aipAppellantManageFeeUpdatePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_not_return_appellant_email_address_from_asylum_case_when_flag_is_disabled() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertFalse(aipAppellantManageFeeUpdatePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipAppellantManageFeeUpdatePersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantManageFeeUpdatePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("dueDate"));
        assertEquals("40.00", personalisation.get("originalTotalFee"));
        assertEquals("20.00", personalisation.get("newTotalFee"));
        assertEquals("20.00", personalisation.get("paymentAmount"));
    }
}
