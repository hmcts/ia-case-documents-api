package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class RespondentUpdateTribunalDecisionRule31PersonalisationEmailTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String respondentUpdateTribunalDecisionRule31EmailTemplateId = "respondentUpdateTribunalDecisionRule31EmailTemplateId";
    private String exUiFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedRespondentEmail = "fake@faketest.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private RespondentUpdateTribunalDecisionRule31PersonalisationEmail respondentUpdateTribunalDecisionRule31PersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        respondentUpdateTribunalDecisionRule31PersonalisationEmail = new RespondentUpdateTribunalDecisionRule31PersonalisationEmail(
            respondentUpdateTribunalDecisionRule31EmailTemplateId,
            exUiFrontendUrl,
            customerServicesProvider,
            emailAddressFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(respondentUpdateTribunalDecisionRule31EmailTemplateId, respondentUpdateTribunalDecisionRule31PersonalisationEmail.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL",
                respondentUpdateTribunalDecisionRule31PersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase))
            .thenReturn(mockedRespondentEmail);

        assertTrue(respondentUpdateTribunalDecisionRule31PersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedRespondentEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> respondentUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_first_check_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("dismissed", "Yes, change decision to Dismissed"),
                List.of(
                        new Value("DISMISSED", "Yes, change decision to Dismissed"),
                        new Value("ALLOWED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation = respondentUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("respondentReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(exUiFrontendUrl, personalisation.get("linkToService"));
        assertEquals("the appeal decision has been changed", personalisation.get("firstBulletPoint"));
        assertEquals("no", personalisation.get("bothChanges"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    void should_return_personalisation_second_check_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("dismissed", "No"),
                List.of(
                        new Value("ALLOWED", "Yes, change decision to Allowed"),
                        new Value("DISMISSED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = respondentUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("respondentReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(exUiFrontendUrl, personalisation.get("linkToService"));
        assertEquals("a new Decision and Reasons document is available to view in the documents tab", personalisation.get("firstBulletPoint"));
        assertEquals("no", personalisation.get("bothChanges"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    void should_return_personalisation_both_checks_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("dismissed", "Yes, change decision to Dismissed"),
                List.of(
                        new Value("DISMISSED", "Yes, change decision to Dismissed"),
                        new Value("ALLOWED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = respondentUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("respondentReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(exUiFrontendUrl, personalisation.get("linkToService"));
        assertEquals("the appeal decision has been changed", personalisation.get("firstBulletPoint"));
        assertEquals("yes", personalisation.get("bothChanges"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

}
