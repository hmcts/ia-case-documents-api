package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter.AppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisationTest {

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
    @Mock
    SystemDateProvider systemDateProvider;

    private Long ccdCaseId = 12345L;
    private String letterTemplateId = "someLetterTemplateId";
    private String appealReferenceNumber = "someAppealRefNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String remissionReasons = "someTestReason";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private int daysAfterRemissionDecision = 14;
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private String originalFeeAmount = "18000";
    private String originalFeeAmountInGbp = "180.00";
    private String onlineCaseReferenceNumber = "1234 5678 9101 1121";

    private AppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of(remissionReasons));
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
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
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(originalFeeAmount));

        appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation = new AppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation(
            letterTemplateId,
            daysAfterRemissionDecision,
            customerServicesProvider,
            systemDateProvider
            );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_REMISSION_PARTIALLY_GRANTED_REFUSED_APPELLANT_LETTER",
            appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_appellant_address_in_correct_format() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_return_legalRep_address_in_correct_format() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepAddressUK is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @ParameterizedTest
    @EnumSource(
            value = RemissionDecision.class,
            names = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_personalisation_when_all_information_given_appellant_in_country(RemissionDecision remissionDecision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation =
            appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(remissionReasons, personalisation.get("RemissionReasons"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        if (remissionDecision == RemissionDecision.PARTIALLY_APPROVED) {
            assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        } else {
            assertEquals(originalFeeAmountInGbp, personalisation.get("feeAmount"));
        }

        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
    }

    @ParameterizedTest
    @EnumSource(
            value = RemissionDecision.class,
            names = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_personalisation_when_all_information_given_appellant_out_of_country(RemissionDecision remissionDecision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(remissionReasons, personalisation.get("RemissionReasons"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_4"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        if (remissionDecision == RemissionDecision.PARTIALLY_APPROVED) {
            assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        } else {
            assertEquals(originalFeeAmountInGbp, personalisation.get("feeAmount"));
        }
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionDecision.class,
        names = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_personalisation_when_all_information_given_legalRep_in_country(RemissionDecision remissionDecision) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation =
            appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(remissionReasons, personalisation.get("RemissionReasons"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        if (remissionDecision == RemissionDecision.PARTIALLY_APPROVED) {
            assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        } else {
            assertEquals(originalFeeAmountInGbp, personalisation.get("feeAmount"));
        }

        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionDecision.class,
        names = {"PARTIALLY_APPROVED", "REJECTED"})
    void should_return_personalisation_when_all_information_given_legalRep_out_of_country(RemissionDecision remissionDecision) {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            appellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(remissionReasons, personalisation.get("RemissionReasons"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        if (remissionDecision == RemissionDecision.PARTIALLY_APPROVED) {
            assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        } else {
            assertEquals(originalFeeAmountInGbp, personalisation.get("feeAmount"));
        }
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
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
