package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalCaseAdjournedWithoutDatePersonalisationTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    AddressUk address;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    private Long ccdCaseId = 12345L;
    private final String customerServicesTelephone = "0300 123 1711";
    private String customerServicesEmail = "example@example.com";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private final String templateName = "templateName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String formattedManchesterHearingCentreAddress = "birmingham";
    private final String listCaseHearingDate = "2023-08-14T14:30:00.000";
    private AppellantInternalCaseAdjournedWithoutDatePersonalisation internalCaseAdjournedWithoutDatePersonalisation;
    private Map<String, String> fieldValuesMap;
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private final String listCaseHearingCentre = HearingCentre.BIRMINGHAM.toString();

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when((customerServicesProvider.getCustomerServicesPersonalisation())).thenReturn(
            Map.of(
                "customerServicesTelephone", customerServicesTelephone,
                "customerServicesEmail", customerServicesEmail
            ));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class)).thenReturn(Optional.of("Reasons"));
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);

        internalCaseAdjournedWithoutDatePersonalisation =
            new AppellantInternalCaseAdjournedWithoutDatePersonalisation(templateName, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalCaseAdjournedWithoutDatePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Assertions.assertEquals(ccdCaseId + "_INTERNAL_CASE_ADJOURN_WITHOUT_DATE_APPELLANT_LETTER",
            internalCaseAdjournedWithoutDatePersonalisation.getReferenceId(ccdCaseId));
    }

    void appellantInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }

    void legalRepInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }

    @Test
    void should_populate_template_correctly_appellant_in_country() {
        appellantInCountryDataSetup();
        fieldValuesMap = internalCaseAdjournedWithoutDatePersonalisation.getPersonalisation(callback);
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValuesMap.get("customerServicesEmail"));
        assertEquals(hearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals("Reasons", fieldValuesMap.get("adjournedHearingReason"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(postCode, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_legalRep_in_country() {
        legalRepInCountryDataSetup();
        fieldValuesMap = internalCaseAdjournedWithoutDatePersonalisation.getPersonalisation(callback);
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValuesMap.get("customerServicesEmail"));
        assertEquals(hearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals("Reasons", fieldValuesMap.get("adjournedHearingReason"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(postCode, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_appellant_out_of_country() {
        appellantOocDataSetupOoc();
        fieldValuesMap = internalCaseAdjournedWithoutDatePersonalisation.getPersonalisation(callback);
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValuesMap.get("customerServicesEmail"));
        assertEquals(hearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals("Reasons", fieldValuesMap.get("adjournedHearingReason"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(oocAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(oocAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(oocAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), fieldValuesMap.get("address_line_4"));
    }

    @Test
    void should_populate_template_correctly_legalRep_out_of_country() {
        legalRepOocDataSetupOoc();
        fieldValuesMap = internalCaseAdjournedWithoutDatePersonalisation.getPersonalisation(callback);
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, fieldValuesMap.get("customerServicesEmail"));
        assertEquals(hearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals("Reasons", fieldValuesMap.get("adjournedHearingReason"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(oocAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(oocAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(oocAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), fieldValuesMap.get("address_line_4"));
    }

    void appellantOocDataSetupOoc() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    void legalRepOocDataSetupOoc() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalCaseAdjournedWithoutDatePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalCaseAdjournedWithoutDatePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepAddressUK is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> internalCaseAdjournedWithoutDatePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }
}
