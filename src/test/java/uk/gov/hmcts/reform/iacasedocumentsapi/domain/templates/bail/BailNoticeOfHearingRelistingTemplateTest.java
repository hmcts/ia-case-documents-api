package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETAINED_LOC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BailNoticeOfHearingRelistingTemplateTest {

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private StringProvider stringProvider;

    private final String applicantGivenNames = "John";
    private final String applicantFamilyName = "Smith";
    private final String homeOfficeReferenceNumber = "123654";
    private final String bailReferenceNumber = "5555-5555-5555-9999";
    private final String applicantPrisonDetails = "applicantPrisonDetails";
    private final String customerServicesEmail = "customer@services.com";
    private final String customerServicesPhone = "111122223333";
    final String legalRepReference = "legalRepReference";

    private final String relistingTemplateName = "TB-IAC-HNO-ENG-bails-notice-of-hearings-relisted.docx";

    private BailNoticeOfHearingRelistingTemplate template;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        template =
            new BailNoticeOfHearingRelistingTemplate(
                relistingTemplateName, customerServicesProvider, stringProvider);
    }

    @Test
    void should_return_relisting_template_name() {

        assertEquals(relistingTemplateName, template.getName());
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        dataSetUp();

        fieldValuesMap = template.mapFieldValues(caseDetails);

        checkCommonFields();
    }

    @Test
    void should_correctly_set_fields() {
        final String hearingDate = "12012024";
        final String hearingTime = "1500";
        final String listingAddress = """
            Nottingham Justice Centre
            Carrington Street
            Nottingham
            NG2 1EE""";
        dataSetUp();
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of("prison"));

        fieldValuesMap = template.mapFieldValues(caseDetails);

        assertEquals(applicantGivenNames, fieldValuesMap.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, fieldValuesMap.get("applicantFamilyName"));
        assertEquals(bailReferenceNumber, fieldValuesMap.get("OnlineCaseReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReference, fieldValuesMap.get("legalRepReference"));
        assertEquals("Dungavel", fieldValuesMap.get("applicantDetainedLoc"));
        assertEquals(applicantPrisonDetails, fieldValuesMap.get("applicantPrisonDetails"));
        assertEquals(listingAddress, fieldValuesMap.get("hearingCentreAddress"));
        assertEquals(hearingDate, fieldValuesMap.get("hearingDate"));
        assertEquals(hearingTime, fieldValuesMap.get("hearingTime"));
        assertEquals(customerServicesEmail, fieldValuesMap.get("customerServicesEmail"));
        assertEquals(customerServicesPhone, fieldValuesMap.get("customerServicesTelephone"));
    }

    private void checkCommonFields() {
        assertTrue(fieldValuesMap.containsKey("applicantGivenNames"));
        assertTrue(fieldValuesMap.containsKey("applicantFamilyName"));
        assertTrue(fieldValuesMap.containsKey("OnlineCaseReferenceNumber"));
        assertTrue(fieldValuesMap.containsKey("homeOfficeReferenceNumber"));
        assertTrue(fieldValuesMap.containsKey("legalRepReference"));
        assertTrue(fieldValuesMap.containsKey("applicantDetainedLoc"));
        assertTrue(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertTrue(fieldValuesMap.containsKey("hearingCentreAddress"));
        assertTrue(fieldValuesMap.containsKey("hearingDate"));
        assertTrue(fieldValuesMap.containsKey("hearingTime"));
        assertTrue(fieldValuesMap.containsKey("customerServicesTelephone"));
        assertTrue(fieldValuesMap.containsKey("customerServicesEmail"));
    }

    void dataSetUp() {
        final String listingLocation = "nottingham";
        final String listingHearingDate = "2024-01-12T15:00:00.969590900";

        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        String applicantDetainedLoc = "immigrationRemovalCentre";
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of(applicantDetainedLoc));
        when(bailCase.read(APPLICANT_PRISON_DETAILS, String.class)).thenReturn(Optional.of(applicantPrisonDetails));
        when(bailCase.read(LISTING_LOCATION, String.class)).thenReturn(Optional.of(listingLocation));
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.of(listingHearingDate));
        when(stringProvider.get("hearingCentreAddress", listingLocation))
            .thenReturn(Optional.of("Nottingham Justice Centre, Carrington Street, Nottingham, NG2 1EE"));
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesPhone);
        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Dungavel"));
    }

}
