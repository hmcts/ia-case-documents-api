package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.refdata.CourtVenue;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BailNoticeOfHearingConditionalBailRelistingTemplateTest {

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
    private final String conditionalBailRelistingTemplateName = "TB-IAC-HNO-ENG-bails-notice-of-hearings-relisted-conditional-bail.docx";

    private CourtVenue newCastle;
    private BailNoticeOfHearingRelistingTemplate template;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        template =
            new BailNoticeOfHearingRelistingTemplate(
                conditionalBailRelistingTemplateName, customerServicesProvider, stringProvider);

        newCastle = new CourtVenue("Newcastle Civil & Family Courts and Tribunals Centre",
                "Newcastle Civil And Family Courts And Tribunals Centre",
                "366796",
                "Open",
                "Y",
                "Y",
                "Barras Bridge, Newcastle-Upon-Tyne",
                "NE1 8QF");
    }

    @Test
    void should_return_relisting_template_name() {

        assertEquals(conditionalBailRelistingTemplateName, template.getName());
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

    @Test
    void should_get_listing_location_address_from_ccd_with_disabled_ref_data_feature() {
        dataSetUp();
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = template.mapFieldValues(caseDetails);

        assertEquals("Nottingham Justice Centre\nCarrington Street\nNottingham\nNG2 1EE",
                fieldValuesMap.get("hearingCentreAddress"));
    }

    @Test
    void should_get_remote_hearing_address_with_enabled_ref_data_feature() {
        dataSetUp();
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        fieldValuesMap = template.mapFieldValues(caseDetails);

        assertEquals("Cloud Video Platform (CVP)",
                fieldValuesMap.get("hearingCentreAddress"));
    }

    @Test
    void should_get_listing_location_address_from_ref_data_with_enabled_ref_data_feature() {
        dataSetUp();
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(REF_DATA_LISTING_LOCATION_DETAIL, CourtVenue.class))
                .thenReturn(Optional.of(newCastle));

        fieldValuesMap = template.mapFieldValues(caseDetails);

        assertEquals("Newcastle Civil And Family Courts And Tribunals Centre, " +
                "Barras Bridge, Newcastle-Upon-Tyne, " +
                "NE1 8QF", fieldValuesMap.get("hearingCentreAddress"));
    }

}
