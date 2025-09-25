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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalRecordOutOfTimeDecisionLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    @Mock
    AddressUk addressLr;
    @Mock
    private SystemDateProvider systemDateProvider;
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
    private String postTown = "Town name";

    private String lrAddressLine1 = "50";
    private String lrAddressLine2 = "Lr Building name";
    private String lrAddressLine3 = "Lr Street name";
    private String lrPostCode = "XX1 2YY";
    private String lrPostTown = "Town name";

    private String occAddressLine1 = "70";
    private String occAddressLine2 = "Building name OCC";
    private String occAddressLine3 = "Street name OCC";
    private String occAddressLine4 = "XXX YYY";
    private String occCountry = "Australia";

    private String lrOccAddressLine1 = "70";
    private String lrOccAddressLine2 = "Lr Building name OCC";
    private String lrOccAddressLine3 = "Lr Street name OCC";
    private String lrOccAddressLine4 = "XXX YYY";
    private String lrOccCountry = "Australia";

    private InternalRecordOutOfTimeDecisionLetterTemplate internalRecordOutOfTimeDecisionLetterTemplate;
    private Map<String, Object> fieldValuesMap;
    private int daysAfterSubmitAppeal = 28;

    @BeforeEach
    public void setUp() {
        internalRecordOutOfTimeDecisionLetterTemplate =
            new InternalRecordOutOfTimeDecisionLetterTemplate(templateName, daysAfterSubmitAppeal, customerServicesProvider, systemDateProvider);
        dataSetup();
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalRecordOutOfTimeDecisionLetterTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));

        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(addressLr));
        when(addressLr.getAddressLine1()).thenReturn(Optional.of(lrAddressLine1));
        when(addressLr.getAddressLine2()).thenReturn(Optional.of(lrAddressLine2));
        when(addressLr.getAddressLine3()).thenReturn(Optional.of(lrAddressLine3));
        when(addressLr.getPostCode()).thenReturn(Optional.of(lrPostCode));
        when(addressLr.getPostTown()).thenReturn(Optional.of(lrPostTown));

        when(asylumCase.read(ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(occAddressLine1));
        when(asylumCase.read(ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(occAddressLine2));
        when(asylumCase.read(ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(occAddressLine3));
        when(asylumCase.read(ADDRESS_LINE_4_ADMIN_J, String.class)).thenReturn(Optional.of(occAddressLine4));
        when(asylumCase.read(COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(new NationalityFieldValue("AU")));

        when(asylumCase.read(OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(lrOccAddressLine1));
        when(asylumCase.read(OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(lrOccAddressLine2));
        when(asylumCase.read(OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(lrOccAddressLine3));
        when(asylumCase.read(OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(lrOccAddressLine4));
        when(asylumCase.read(OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(new NationalityFieldValue("AU")));
    }

    @Test
    void should_populate_template_correctly() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        fieldValuesMap = internalRecordOutOfTimeDecisionLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(systemDateProvider.dueDate(28), fieldValuesMap.get("fourWeeksAfterSubmitDate"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(postTown, fieldValuesMap.get("address_line_4"));
        assertEquals(postCode, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_with_appellant_occ() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = internalRecordOutOfTimeDecisionLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(systemDateProvider.dueDate(28), fieldValuesMap.get("fourWeeksAfterSubmitDate"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(occAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(occAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(occAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(occAddressLine4, fieldValuesMap.get("address_line_4"));
        assertEquals(occCountry, fieldValuesMap.get("address_line_5"));
    }

    @Test
    void should_populate_template_correctly_with_lr() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        fieldValuesMap = internalRecordOutOfTimeDecisionLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(systemDateProvider.dueDate(28), fieldValuesMap.get("fourWeeksAfterSubmitDate"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(lrAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(lrAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(lrAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(lrPostTown, fieldValuesMap.get("address_line_4"));
        assertEquals(lrPostCode, fieldValuesMap.get("address_line_5"));
    }


    @Test
    void should_populate_template_correctly_with_lr_occ() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = internalRecordOutOfTimeDecisionLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(systemDateProvider.dueDate(28), fieldValuesMap.get("fourWeeksAfterSubmitDate"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(lrOccAddressLine1, fieldValuesMap.get("address_line_1"));
        assertEquals(lrOccAddressLine2, fieldValuesMap.get("address_line_2"));
        assertEquals(lrOccAddressLine3, fieldValuesMap.get("address_line_3"));
        assertEquals(lrOccAddressLine4, fieldValuesMap.get("address_line_4"));
        assertEquals(lrOccCountry, fieldValuesMap.get("address_line_5"));
    }

}