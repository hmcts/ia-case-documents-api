package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantUpdateTribunalDecisionRule31PersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AppellantUpdateTribunalDecisionRule31PersonalisationEmailTest {
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
    private final String updateTribunalDecisionRule31DecisionTemplateId = "decisionTemplateId";
    private final String updateTribunalDecisionRule31DocumentTemplateId = "documentTemplateId";
    private final String updateTribunalDecisionRule31BothTemplateId  = "bothTemplateId";
    private final String allowed = "Allowed";
    private final String dismissed = "Dismissed";
    private final String days28 = "28 days";
    private final String days14 = "14 days";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantEmail = "fake@faketest.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private AppellantUpdateTribunalDecisionRule31PersonalisationEmail appellantUpdateTribunalDecisionRule31PersonalisationEmail;
    private final DynamicList dynamicAllowedDecisionList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
    );
    private final DynamicList dynamicDismissedDecisionList = new DynamicList(
            new Value("dismissed", "No"),
            newArrayList()
    );

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

        appellantUpdateTribunalDecisionRule31PersonalisationEmail = new AppellantUpdateTribunalDecisionRule31PersonalisationEmail(
            updateTribunalDecisionRule31DecisionTemplateId,
            updateTribunalDecisionRule31DocumentTemplateId,
            updateTribunalDecisionRule31BothTemplateId,
            iaAipFrontendUrl,
            customerServicesProvider,
            recipientsFinder
        );
    }

    @Test
    void should_return_template_id_given_updated_decision() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicDismissedDecisionList));
        assertEquals(updateTribunalDecisionRule31DocumentTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(updateTribunalDecisionRule31BothTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(updateTribunalDecisionRule31DecisionTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationEmail.getTemplateId(asylumCase));

    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL",
            appellantUpdateTribunalDecisionRule31PersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmail));

        assertTrue(appellantUpdateTribunalDecisionRule31PersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantUpdateTribunalDecisionRule31PersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_decision_updated(YesOrNo outOfCountry) {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
                .thenReturn(Optional.of(outOfCountry));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(dismissed, personalisation.get("oldDecision"));
        assertEquals(allowed, personalisation.get("newDecision"));
        if (outOfCountry.equals(YesOrNo.YES)) {
            assertEquals(days28, personalisation.get("period"));
        } else {
            assertEquals(days14, personalisation.get("period"));
        }

    }

    @Test
    void should_return_personalisation_when_all_information_given_decision_not_updated() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

}
