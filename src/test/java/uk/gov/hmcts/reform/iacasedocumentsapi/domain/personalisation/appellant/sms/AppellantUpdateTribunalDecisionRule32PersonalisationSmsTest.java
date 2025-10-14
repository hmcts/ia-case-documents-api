package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantUpdateTribunalDecisionRule32PersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

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
@SuppressWarnings("unchecked")
class AppellantUpdateTribunalDecisionRule32PersonalisationSmsTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String appellanttUpdateTribunalDecisionRule32SmsTemplateId = "appellanttUpdateTribunalDecisionRule32SmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private AppellantUpdateTribunalDecisionRule32PersonalisationSms appellantUpdateTribunalDecisionRule32PersonalisationSms;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantUpdateTribunalDecisionRule32PersonalisationSms = new AppellantUpdateTribunalDecisionRule32PersonalisationSms(
            appellanttUpdateTribunalDecisionRule32SmsTemplateId,
            iaAipFrontendUrl,
            customerServicesProvider,
            recipientsFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(appellanttUpdateTribunalDecisionRule32SmsTemplateId, appellantUpdateTribunalDecisionRule32PersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_32_SMS",
                appellantUpdateTribunalDecisionRule32PersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantUpdateTribunalDecisionRule32PersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantUpdateTribunalDecisionRule32PersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantUpdateTribunalDecisionRule32PersonalisationSms.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkAipTimeline"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

}
