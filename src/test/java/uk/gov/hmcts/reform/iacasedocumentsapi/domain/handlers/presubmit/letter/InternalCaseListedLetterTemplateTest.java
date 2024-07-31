package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalCaseListedLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    @Mock
    private StringProvider stringProvider;
    private final String telephoneNumber = "0300 123 1711";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private final String templateName = "templateName";
    private final String logo = "[userImage:hmcts.png]";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private Nationality nationalityOoc = Nationality.ES;
    private String postTown = "Town name";
    private String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private String formattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private final String listCaseHearingDate = "2023-08-14T14:30:00.000";
    private final String formattedListCaseHearingDate = "14 August 2023";
    private String formattedListCaseHearingTime = "1430";
    private InternalCaseListedLetterTemplate internalCaseListedLetterTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        internalCaseListedLetterTemplate =
            new InternalCaseListedLetterTemplate(templateName, customerServicesProvider, stringProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalCaseListedLetterTemplate.getName());
    }

    @Test
    void should_populate_template_correctly_appellant_in_uk() {
        dataSetup(true);

        fieldValuesMap = internalCaseListedLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(formattedListCaseHearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals(formattedListCaseHearingTime, fieldValuesMap.get("hearingTime"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(postCode, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_appellant_ooc() {
        dataSetup(false);

        fieldValuesMap = internalCaseListedLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(formattedListCaseHearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals(formattedListCaseHearingTime, fieldValuesMap.get("hearingTime"));
        assertEquals(formattedManchesterHearingCentreAddress, fieldValuesMap.get("hearingLocation"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(nationalityOoc.toString(), fieldValuesMap.get("address_line_5"));
    }

    void dataSetup(boolean appellantInUk) {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        if (appellantInUk) {
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
            when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
            when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
            when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
            when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
            when(address.getPostTown()).thenReturn(Optional.of(postTown));
            when(address.getPostCode()).thenReturn(Optional.of(postCode));
        } else {
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
            when(asylumCase.read(ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine1));
            when(asylumCase.read(ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine2));
            when(asylumCase.read(ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine3));
            when(asylumCase.read(ADDRESS_LINE_4_ADMIN_J, String.class)).thenReturn(Optional.of(postTown));
            when(asylumCase.read(COUNTRY_OOC_ADMIN_J, Nationality.class)).thenReturn(Optional.of(nationalityOoc));
        }
    }
}
