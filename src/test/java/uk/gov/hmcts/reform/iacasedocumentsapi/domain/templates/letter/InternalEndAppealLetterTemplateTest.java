package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalEndAppealLetterTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    private final String telephoneNumber = "0300 123 1711";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private String appealEndDate = "2023-07-22";
    private String approverType = "Legal Officer";
    private final String templateName = "templateName";
    private final String logo = "[userImage:hmcts.png]";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private InternalEndAppealLetterTemplate internalEndAppealLetterTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        internalEndAppealLetterTemplate =
            new InternalEndAppealLetterTemplate(templateName, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalEndAppealLetterTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(appealEndDate));
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(approverType));
        when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }

    void dataSetupOoc() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(appealEndDate));
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(approverType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(COUNTRY_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    @Test
    void should_populate_template_correctly_in_country() {
        dataSetup();
        fieldValuesMap = internalEndAppealLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals("22 July 2023", fieldValuesMap.get("endAppealDate"));
        assertEquals(approverType, fieldValuesMap.get("decisionMaker"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(postCode, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_out_of_country() {
        dataSetupOoc();
        fieldValuesMap = internalEndAppealLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals("22 July 2023", fieldValuesMap.get("endAppealDate"));
        assertEquals(approverType, fieldValuesMap.get("decisionMaker"));
        assertEquals(oocAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(oocAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(oocAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), fieldValuesMap.get("address_line_4"));
    }
}
