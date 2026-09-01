
package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADDRESS_LINE_4_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_ADDRESS_LINE_4;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InternalRemove24wTimeframeLetterTemplateTest {

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
    private MakeAnApplication application;
    private final String telephoneNumber = "0300 123 1711";
    private final String email = "email@test.com";
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

    private InternalRemove24wTimeframeLetterTemplate template;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        template =
            new InternalRemove24wTimeframeLetterTemplate(templateName, customerServicesProvider);
        dataSetup();
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);
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

        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("123"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(List.of(new IdValue<>("123", application))));
    }

    @Test
    void should_populate_template_correctly() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.of("2024-06-01"));

        fieldValuesMap = template.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(email, fieldValuesMap.get("customerServicesEmail"));
        assertNull(fieldValuesMap.get("legalRepRefPlusTitle"));
        assertTrue(((String) fieldValuesMap.get("completeCaseReviewDependentContent"))
            .contains("1 Jun 2024"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(appellantGivenNames + " " + appellantFamilyName, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_3"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_4"));
        assertEquals(postTown, fieldValuesMap.get("address_line_5"));
        assertEquals(postCode, fieldValuesMap.get("address_line_6"));
    }

    @Test
    void should_populate_template_correctly_with_lr() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("123"));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.empty());
        fieldValuesMap = template.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertNull(fieldValuesMap.get("completeCaseReviewDependentContent"));
        assertEquals(email, fieldValuesMap.get("customerServicesEmail"));
        assertEquals("\nLegal Rep reference: 123", fieldValuesMap.get("legalRepRefPlusTitle"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(appellantGivenNames + " " + appellantFamilyName, fieldValuesMap.get("address_line_1"));
        assertEquals(addressLine1, fieldValuesMap.get("address_line_2"));
        assertEquals(addressLine2, fieldValuesMap.get("address_line_3"));
        assertEquals(addressLine3, fieldValuesMap.get("address_line_4"));
        assertEquals(postTown, fieldValuesMap.get("address_line_5"));
        assertEquals(postCode, fieldValuesMap.get("address_line_6"));
    }
}
