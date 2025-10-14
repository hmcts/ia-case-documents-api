package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter.AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisationTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    private Long ccdCaseId = 12345L;
    private String letterTemplateId = "someLetterTemplateId";
    private String appealReferenceNumber = "someAppealRefNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private String decisionMaker = "Legal Officer";
    private String reinstateReason = "Example reason";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private final String allowed = "Allowed";
    private final String dismissed = "Dismissed";
    private final DynamicList dynamicAllowedDecisionList = new DynamicList(
        new Value("allowed", "Yes, change decision to Allowed"),
        newArrayList()
    );
    private final DynamicList dynamicDismissedDecisionList = new DynamicList(
        new Value("dismissed", "No"),
        newArrayList()
    );
    private AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation;


    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));

        appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation = new AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation(
            letterTemplateId, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_UPDATE_TRIBUNAL_DECISION_RULE31_LETTER",
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_appellant_address_in_correct_format() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_return_legalRep_address_in_correct_format() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepAddressUK is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_appellant_in_country(String decision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(decision.equals(allowed) ? dismissed : allowed, personalisation.get("oldDecision"));
        assertEquals(decision, personalisation.get("newDecision"));
        assertEquals("14 days", personalisation.get("period"));

    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_appellant_ooc(String decision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_4"));
        assertEquals(decision.equals(allowed) ? dismissed : allowed, personalisation.get("oldDecision"));
        assertEquals(decision, personalisation.get("newDecision"));
        assertEquals("28 days", personalisation.get("period"));
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_decision_not_updated() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_4"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_legalRep_in_country(String decision) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(decision.equals(allowed) ? dismissed : allowed, personalisation.get("oldDecision"));
        assertEquals(decision, personalisation.get("newDecision"));
        assertEquals("14 days", personalisation.get("period"));

    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_legalRep_ooc(String decision) {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_5"));
        assertEquals(decision.equals(allowed) ? dismissed : allowed, personalisation.get("oldDecision"));
        assertEquals(decision, personalisation.get("newDecision"));
        assertEquals("28 days", personalisation.get("period"));
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_decision_not_updated() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

    private void legalRepOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    private void legalRepInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }

}
