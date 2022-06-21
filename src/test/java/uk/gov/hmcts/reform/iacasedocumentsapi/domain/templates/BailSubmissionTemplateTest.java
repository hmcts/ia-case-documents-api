package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.InterpreterLanguage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.BailSubmissionTemplate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailSubmissionTemplateTest {
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private PriorApplication priorApplication;

    private final String templateName = "BAIL_SUBMISSION_TEMPLATE.docx";
    private String applicantGivenNames = "John";
    private String applicantFamilyName = "Smith";
    private String applicationSubmittedBy = "Legal Representative";
    private String applicantDateOfBirth = "1999-12-31";
    private String applicantGender = "Male";
    private String applicantMobileNumber = "07777777777";
    private String homeOfficeReferenceNumber = "123654";
    private String applicantDetainedLoc = "prison";
    private String applicantPrisonDetails = "4568";
    private String prisonName = "Aylesbury";
    private String applicantArrivalInUkDate = "2001-02-22";
    private String hasAppealHearingPending = "Yes";
    private String appealReferenceNumber = "8989889";
    private String hasPreviousBailApplication = "Yes";
    private String supporterGivenNames = "Supporter1";
    private String supporterFamilyNames = "Family";
    private String supporterTelephoneNumber = "5698565666";
    private String supporterMobileNumber = "464464444";
    private String supporterEmailAddress = "dhsk@gsg.com";
    private String supporterDob = "1998-02-15";
    private String supporterRelation = "Brother";
    private String supporterOccupation = "Doctor";
    private String supporterImmigration = "Resident";
    private String supporterHasPassport = "Yes";
    private String financialAmountSupporterUndertakes = "2000";
    private String groundsForBailReasons = "Grounds for bails";
    private String legalRepCompany = "COMPANY NAME";
    private String legalRepName = "REP NAME";
    private String legalRepEmail = "email@company.com";
    private String legalRepPhone = "07777777777";
    private String legalRepReference = "TREF09";
    private String applicantAddressLine1 = "123 Some Street";
    private String applicantAddressLine2 = "Some Area";
    private String applicantAddressLine3 = "Some District";
    private String applicantAddressPostTown = "Some Town";
    private String applicantAddressCounty = "South";
    private String applicantAddressPostCode = "AB1 2CD";
    private String applicantAddressCountry = "Iceland";
    private String supporterAddressLine1 = "123 Test Street";
    private String supporterAddressLine2 = "Test Area";
    private String supporterAddressLine3 = "Test District";
    private String supporterAddressPostTown = "Test Town";
    private String supporterAddressCounty = "South";
    private String supporterAddressPostCode = "AB1 2CD";
    private String supporterAddressCountry = "UK";
    private LocalDateTime createdDate = LocalDateTime.parse("2020-12-31T12:34:56");

    private AddressUk addressUk = new AddressUk(
        applicantAddressLine1,
        applicantAddressLine2,
        applicantAddressLine3,
        applicantAddressPostTown,
        applicantAddressCounty,
        applicantAddressPostCode,
        applicantAddressCountry
    );

    private AddressUk supporterAddressUk = new AddressUk(
        supporterAddressLine1,
        supporterAddressLine2,
        supporterAddressLine3,
        supporterAddressPostTown,
        supporterAddressCounty,
        supporterAddressPostCode,
        supporterAddressCountry
    );

    private List<IdValue<NationalityFieldValue>> applicantNationalities =
        Arrays.asList(
            new IdValue<>("1", new NationalityFieldValue("American"))
        );

    private List<IdValue<NationalityFieldValue>> supporterNationalities =
        Arrays.asList(
            new IdValue<>("1", new NationalityFieldValue("Algerian"))
        );

    private List<IdValue<InterpreterLanguage>> interpreterLanguages =
        Arrays.asList(
            new IdValue<>("1", new InterpreterLanguage("Arabic", "NA"))
        );

    private BailSubmissionTemplate bailSubmissionTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        bailSubmissionTemplate = new BailSubmissionTemplate(templateName);
    }

    @Test
    void should_set_sentBy_properly_for_admin() {
        dataSetUp();
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        List<String> validSubmissionsBy = Arrays.asList("Applicant", "Legal Representative", "Home Office");
        for (String submittedBy : validSubmissionsBy) {
            when(bailCase.read(SENT_BY_CHECKLIST, String.class)).thenReturn(Optional.of(submittedBy));
            fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
            assertEquals(Optional.of(submittedBy), fieldValuesMap.get("applicationSubmittedBy"));
        }
    }

    @Test
    void should_not_set_previous_application_reference_if_not_present() {
        dataSetUp();
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("No"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("previousBailApplicationNumber"));
    }

    @Test
    void should_not_set_previous_appeal_reference_if_not_present() {
        dataSetUp();
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of("No"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("appealReferenceNumber"));
    }

    @Test
    void should_set_disability_details_if_present() {
        dataSetUp();
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("applicantDisabilityDetails"));
    }

    @Test
    void should_set_prison_details_if_detained_in_prison() {
        dataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("prisonName"));
        assertTrue(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertFalse(fieldValuesMap.containsKey("ircName"));
    }

    @Test
    void should_set_prison_details_if_detained_in_irc() {
        dataSetUp();
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Derwentside"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertFalse(fieldValuesMap.containsKey("prisonName"));
        assertFalse(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertTrue(fieldValuesMap.containsKey("ircName"));
    }

    @Test
    void should_not_set_supporter_2_data_if_not_present() {
        dataSetUp();
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("supporter2GivenNames"));
        assertFalse(fieldValuesMap.containsKey("supporter2FamilyNames"));
        assertFalse(fieldValuesMap.containsKey("supporter2AddressDetails"));
        assertFalse(fieldValuesMap.containsKey("supporter2TelephoneNumber"));
        assertFalse(fieldValuesMap.containsKey("supporter2MobileNumber"));
        assertFalse(fieldValuesMap.containsKey("supporter2EmailAddress"));
        assertFalse(fieldValuesMap.containsKey("supporter2DOB"));
        assertFalse(fieldValuesMap.containsKey("supporter2Relation"));
        assertFalse(fieldValuesMap.containsKey("supporter2Occupation"));
        assertFalse(fieldValuesMap.containsKey("supporter2Immigration"));
        assertFalse(fieldValuesMap.containsKey("supporter2Nationalities"));
        assertFalse(fieldValuesMap.containsKey("supporter2HasPassport"));
        assertFalse(fieldValuesMap.containsKey("supporter2Passport"));
        assertFalse(fieldValuesMap.containsKey("financialAmountSupporter2Undertakes"));
    }

    @Test
    void should_set_supporter_1_2_3_data_if_present() {
        dataSetUp();
        supporter2DataSetUp();
        supporter3DataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        checkCommonFields();
        assertSupporter2Fields();
        assertSupporter3Fields();
        assertFalse(fieldValuesMap.containsKey("supporter4GivenNames"));
        assertFalse(fieldValuesMap.containsKey("supporter4FamilyNames"));
        assertFalse(fieldValuesMap.containsKey("supporter4AddressDetails"));
        assertFalse(fieldValuesMap.containsKey("supporter4TelephoneNumber"));
        assertFalse(fieldValuesMap.containsKey("supporter4MobileNumber"));
        assertFalse(fieldValuesMap.containsKey("supporter4EmailAddress"));
        assertFalse(fieldValuesMap.containsKey("supporter4DOB"));
        assertFalse(fieldValuesMap.containsKey("supporter4Relation"));
        assertFalse(fieldValuesMap.containsKey("supporter4Occupation"));
        assertFalse(fieldValuesMap.containsKey("supporter4Immigration"));
        assertFalse(fieldValuesMap.containsKey("supporter4Nationalities"));
        assertFalse(fieldValuesMap.containsKey("supporter4HasPassport"));
        assertFalse(fieldValuesMap.containsKey("supporter4Passport"));
        assertFalse(fieldValuesMap.containsKey("financialAmountSupporter4Undertakes"));
    }

    @Test
    void should_set_supporter_2_3_4_data_if_present() {
        dataSetUp();
        supporter2DataSetUp();
        supporter3DataSetUp();
        supporter4DataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        checkCommonFields();
        assertSupporter2Fields();
        assertSupporter3Fields();
        assertSupporter4Fields();
    }

    @Test
    void should_be_tolerant_to_empty_data() {
        dataSetUp();
        when(bailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_PHONE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("", fieldValuesMap.get("legalRepCompany"));
        assertEquals("", fieldValuesMap.get("legalRepName"));
        assertEquals("", fieldValuesMap.get("legalRepEmail"));
        assertEquals("", fieldValuesMap.get("legalRepPhone"));
        assertEquals("", fieldValuesMap.get("legalRepReference"));
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, bailSubmissionTemplate.getName());
    }

    /*
    Scenario - Application submitted by LR with one Financial Condition Supporter
    with interpreter languages
    */
    @Test
    void should_set_the_fields_for_application_submittedBy_LR() {
        dataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("legalRepCompany"));
        assertTrue(fieldValuesMap.containsKey("legalRepName"));
        assertTrue(fieldValuesMap.containsKey("legalRepEmail"));
        assertTrue(fieldValuesMap.containsKey("legalRepPhone"));
        assertTrue(fieldValuesMap.containsKey("legalRepReference"));
    }


    /*
    Scenario - Application submitted by HO with one Financial Condition Supporter
    with interpreter languages
    */
    @Test
    void should_set_the_fields_for_application_submittedBy_HO() {
        dataSetUp();
        when(bailCase.read(IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        checkCommonFields();
        assertEquals("Home Office", fieldValuesMap.get("applicationSubmittedBy"));
        assertFalse(fieldValuesMap.containsKey("legalRepCompany"));
        assertFalse(fieldValuesMap.containsKey("legalRepName"));
        assertFalse(fieldValuesMap.containsKey("legalRepEmail"));
        assertFalse(fieldValuesMap.containsKey("legalRepPhone"));
        assertFalse(fieldValuesMap.containsKey("legalRepReference"));
    }

    @Test
    void should_set_gender_details_if_gender_others() {
        dataSetUp();
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of("Other"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("applicantOtherGenderDetails"));
    }

    @Test
    void should_not_set_gender_details_if_gender_male() {
        dataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertFalse(fieldValuesMap.containsKey("applicantOtherGenderDetails"));
    }

    @Test
    void should_set_hasPreviousBailApplication_for_dontknow() {
        dataSetUp();
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("DontKnow"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("Don't Know", fieldValuesMap.get("hasPreviousBailApplication"));
    }

    @Test
    void should_set_hasPreviousBailApplication_for_YesWithoutApplicationNumber() {
        dataSetUp();
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("YesWithoutApplicationNumber"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("Yes Without Application Number", fieldValuesMap.get("hasPreviousBailApplication"));
    }

    @Test
    void should_not_set_passportNumber_if_field_is_empty() {
        dataSetUp();
        when(bailCase.read(SUPPORTER_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_PASSPORT, String.class)).thenReturn(Optional.empty());
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        assertFalse(fieldValuesMap.containsKey("supporterPassport"));
    }

    @Test
    void should_set_LR_Details_For_Application_SubmittedBy_Applicant() {
        dataSetUp();
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SENT_BY_CHECKLIST, String.class)).thenReturn(Optional.of("Applicant"));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals(YesOrNo.YES, fieldValuesMap.get("isLegallyRepresentedForFlag"));
        assertTrue(fieldValuesMap.containsKey("legalRepCompany"));
        assertTrue(fieldValuesMap.containsKey("legalRepName"));
        assertTrue(fieldValuesMap.containsKey("legalRepEmail"));
    }

    @Test
    void should_not_show_previous_application_details_if_prior_applications_dont_exist() {
        dataSetUp();
        when(bailCase.read(PRIOR_APPLICATIONS, PriorApplication.class)).thenReturn(Optional.of(priorApplication));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("showPreviousApplicationSection"));
    }

    //Helper method for common assertions
    private void checkCommonFields() {
        assertTrue(fieldValuesMap.containsKey("applicantGivenNames"));
        assertTrue(fieldValuesMap.containsKey("applicantFamilyName"));
        assertTrue(fieldValuesMap.containsKey("applicationSubmittedBy"));
        assertTrue(fieldValuesMap.containsKey("applicantDateOfBirth"));
        assertTrue(fieldValuesMap.containsKey("applicantGender"));
        assertTrue(fieldValuesMap.containsKey("applicantNationalities"));
        assertTrue(fieldValuesMap.containsKey("applicantHasMobile"));
        assertTrue(fieldValuesMap.containsKey("homeOfficeReferenceNumber"));
        assertTrue(fieldValuesMap.containsKey("applicantDetainedLoc"));
        assertTrue(fieldValuesMap.containsKey("applicantArrivalInUKDate"));
        assertTrue(fieldValuesMap.containsKey("hasAppealHearingPending"));
        assertTrue(fieldValuesMap.containsKey("applicantHasAddress"));
        assertTrue(fieldValuesMap.containsKey("applicantAddress"));
        assertTrue(fieldValuesMap.containsKey("hasPreviousBailApplication"));
        assertTrue(fieldValuesMap.containsKey("applicantBeenRefusedBail"));
        assertTrue(fieldValuesMap.containsKey("agreesToBoundByFinancialCond"));
        assertTrue(fieldValuesMap.containsKey("hasFinancialCondSupporter"));
        assertTrue(fieldValuesMap.containsKey("supporterGivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporterFamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporterAddressDetails"));
        assertTrue(fieldValuesMap.containsKey("supporterTelephoneNumber"));
        assertTrue(fieldValuesMap.containsKey("supporterMobileNumber"));
        assertTrue(fieldValuesMap.containsKey("supporterEmailAddress"));
        assertTrue(fieldValuesMap.containsKey("supporterDOB"));
        assertTrue(fieldValuesMap.containsKey("supporterRelation"));
        assertTrue(fieldValuesMap.containsKey("supporterOccupation"));
        assertTrue(fieldValuesMap.containsKey("supporterImmigration"));
        assertTrue(fieldValuesMap.containsKey("supporterNationalities"));
        assertTrue(fieldValuesMap.containsKey("supporterHasPassport"));
        assertTrue(fieldValuesMap.containsKey("supporterPassport"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporterUndertakes"));
        assertTrue(fieldValuesMap.containsKey("groundsForBailReasons"));
        assertTrue(fieldValuesMap.containsKey("transferBailManagementYesOrNo"));
        assertTrue(fieldValuesMap.containsKey("groundsForBailProvideEvidenceOption"));
        assertTrue(fieldValuesMap.containsKey("interpreterYesNo"));
        assertTrue(fieldValuesMap.containsKey("interpreterLanguages"));
        assertTrue(fieldValuesMap.containsKey("applicantDisability1"));
        assertTrue(fieldValuesMap.containsKey("videoHearing1"));
        assertTrue(fieldValuesMap.containsKey("isLegallyRepresentedForFlag"));
        assertTrue(fieldValuesMap.containsKey("showPreviousApplicationSection"));
    }

    private void assertSupporter2Fields() {
        assertTrue(fieldValuesMap.containsKey("hasFinancialCondSupporter2"));
        assertTrue(fieldValuesMap.containsKey("supporter2GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter2FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter2AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("supporter2TelephoneNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter2MobileNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter2EmailAddress"));
        assertTrue(fieldValuesMap.containsKey("supporter2DOB"));
        assertTrue(fieldValuesMap.containsKey("supporter2Relation"));
        assertTrue(fieldValuesMap.containsKey("supporter2Occupation"));
        assertTrue(fieldValuesMap.containsKey("supporter2Immigration"));
        assertTrue(fieldValuesMap.containsKey("supporter2Nationalities"));
        assertTrue(fieldValuesMap.containsKey("supporter2HasPassport"));
        assertTrue(fieldValuesMap.containsKey("supporter2Passport"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter2Undertakes"));
    }

    private void assertSupporter3Fields() {
        assertTrue(fieldValuesMap.containsKey("hasFinancialCondSupporter3"));
        assertTrue(fieldValuesMap.containsKey("supporter3GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter3FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter3AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("supporter3TelephoneNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter3MobileNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter3EmailAddress"));
        assertTrue(fieldValuesMap.containsKey("supporter3DOB"));
        assertTrue(fieldValuesMap.containsKey("supporter3Relation"));
        assertTrue(fieldValuesMap.containsKey("supporter3Occupation"));
        assertTrue(fieldValuesMap.containsKey("supporter3Immigration"));
        assertTrue(fieldValuesMap.containsKey("supporter3Nationalities"));
        assertTrue(fieldValuesMap.containsKey("supporter3HasPassport"));
        assertTrue(fieldValuesMap.containsKey("supporter3Passport"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter3Undertakes"));
    }

    private void assertSupporter4Fields() {
        assertTrue(fieldValuesMap.containsKey("hasFinancialCondSupporter4"));
        assertTrue(fieldValuesMap.containsKey("supporter4GivenNames"));
        assertTrue(fieldValuesMap.containsKey("supporter4FamilyNames"));
        assertTrue(fieldValuesMap.containsKey("supporter4AddressDetails"));
        assertTrue(fieldValuesMap.containsKey("supporter4TelephoneNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter4MobileNumber"));
        assertTrue(fieldValuesMap.containsKey("supporter4EmailAddress"));
        assertTrue(fieldValuesMap.containsKey("supporter4DOB"));
        assertTrue(fieldValuesMap.containsKey("supporter4Relation"));
        assertTrue(fieldValuesMap.containsKey("supporter4Occupation"));
        assertTrue(fieldValuesMap.containsKey("supporter4Immigration"));
        assertTrue(fieldValuesMap.containsKey("supporter4Nationalities"));
        assertTrue(fieldValuesMap.containsKey("supporter4HasPassport"));
        assertTrue(fieldValuesMap.containsKey("supporter4Passport"));
        assertTrue(fieldValuesMap.containsKey("financialAmountSupporter4Undertakes"));
    }

    // Helper method to set the common data
    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(SENT_BY_CHECKLIST, String.class)).thenReturn(Optional.of(applicationSubmittedBy));
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(applicantDateOfBirth));
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of(applicantGender));
        when(bailCase.read(APPLICANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));
        when(bailCase.read(APPLICANT_NATIONALITIES)).thenReturn(Optional.of(applicantNationalities));
        when(bailCase.read(APPLICANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.of(applicantMobileNumber));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of(applicantDetainedLoc));
        when(bailCase.read(APPLICANT_PRISON_DETAILS, String.class)).thenReturn(Optional.of(applicantPrisonDetails));
        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of(prisonName));
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.of(applicantArrivalInUkDate));
        when(bailCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of(hasAppealHearingPending));
        when(bailCase.read(APPLICANT_GENDER, String.class)).thenReturn(Optional.of(applicantGender));
        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of(hasPreviousBailApplication));
        when(bailCase.read(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(SUPPORTER_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(SUPPORTER_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of(supporterTelephoneNumber));
        when(bailCase.read(SUPPORTER_MOBILE_NUMBER)).thenReturn(Optional.of(supporterMobileNumber));
        when(bailCase.read(SUPPORTER_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(supporterEmailAddress));
        when(bailCase.read(SUPPORTER_DOB, String.class)).thenReturn(Optional.of(supporterDob));
        when(bailCase.read(SUPPORTER_RELATION, String.class)).thenReturn(Optional.of(supporterRelation));
        when(bailCase.read(SUPPORTER_OCCUPATION, String.class)).thenReturn(Optional.of(supporterOccupation));
        when(bailCase.read(SUPPORTER_IMMIGRATION, String.class)).thenReturn(Optional.of(supporterImmigration));
        when(bailCase.read(SUPPORTER_NATIONALITY)).thenReturn(Optional.of(supporterNationalities));
        when(bailCase.read(SUPPORTER_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_PASSPORT, String.class)).thenReturn(Optional.of(supporterHasPassport));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(bailCase.read(GROUNDS_FOR_BAIL_REASONS, String.class)).thenReturn(Optional.of(groundsForBailReasons));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(INTERPRETER_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(INTERPRETER_LANGUAGES)).thenReturn(Optional.of(interpreterLanguages));
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(VIDEO_HEARING1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(legalRepCompany));
        when(bailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmail));
        when(bailCase.read(LEGAL_REP_PHONE, String.class)).thenReturn(Optional.of(legalRepPhone));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
    }

    private void supporter2DataSetUp() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_2_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_2_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_2_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(SUPPORTER_2_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of(supporterTelephoneNumber));
        when(bailCase.read(SUPPORTER_2_MOBILE_NUMBER)).thenReturn(Optional.of(supporterMobileNumber));
        when(bailCase.read(SUPPORTER_2_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(supporterEmailAddress));
        when(bailCase.read(SUPPORTER_2_DOB, String.class)).thenReturn(Optional.of(supporterDob));
        when(bailCase.read(SUPPORTER_2_RELATION, String.class)).thenReturn(Optional.of(supporterRelation));
        when(bailCase.read(SUPPORTER_2_OCCUPATION, String.class)).thenReturn(Optional.of(supporterOccupation));
        when(bailCase.read(SUPPORTER_2_IMMIGRATION, String.class)).thenReturn(Optional.of(supporterImmigration));
        when(bailCase.read(SUPPORTER_2_NATIONALITY)).thenReturn(Optional.of(supporterNationalities));
        when(bailCase.read(SUPPORTER_2_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_2_PASSPORT, String.class)).thenReturn(Optional.of(supporterHasPassport));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes));
    }

    private void supporter3DataSetUp() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_3_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_3_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_3_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(SUPPORTER_3_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of(supporterTelephoneNumber));
        when(bailCase.read(SUPPORTER_3_MOBILE_NUMBER)).thenReturn(Optional.of(supporterMobileNumber));
        when(bailCase.read(SUPPORTER_3_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(supporterEmailAddress));
        when(bailCase.read(SUPPORTER_3_DOB, String.class)).thenReturn(Optional.of(supporterDob));
        when(bailCase.read(SUPPORTER_3_RELATION, String.class)).thenReturn(Optional.of(supporterRelation));
        when(bailCase.read(SUPPORTER_3_OCCUPATION, String.class)).thenReturn(Optional.of(supporterOccupation));
        when(bailCase.read(SUPPORTER_3_IMMIGRATION, String.class)).thenReturn(Optional.of(supporterImmigration));
        when(bailCase.read(SUPPORTER_3_NATIONALITY)).thenReturn(Optional.of(supporterNationalities));
        when(bailCase.read(SUPPORTER_3_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_3_PASSPORT, String.class)).thenReturn(Optional.of(supporterHasPassport));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes));
    }

    private void supporter4DataSetUp() {
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_4_GIVEN_NAMES, String.class)).thenReturn(Optional.of(supporterGivenNames));
        when(bailCase.read(SUPPORTER_4_FAMILY_NAMES, String.class)).thenReturn(Optional.of(supporterFamilyNames));
        when(bailCase.read(SUPPORTER_4_ADDRESS_DETAILS, AddressUk.class)).thenReturn(Optional.of(supporterAddressUk));
        when(bailCase.read(SUPPORTER_4_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of(supporterTelephoneNumber));
        when(bailCase.read(SUPPORTER_4_MOBILE_NUMBER)).thenReturn(Optional.of(supporterMobileNumber));
        when(bailCase.read(SUPPORTER_4_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(supporterEmailAddress));
        when(bailCase.read(SUPPORTER_4_DOB, String.class)).thenReturn(Optional.of(supporterDob));
        when(bailCase.read(SUPPORTER_4_RELATION, String.class)).thenReturn(Optional.of(supporterRelation));
        when(bailCase.read(SUPPORTER_4_OCCUPATION, String.class)).thenReturn(Optional.of(supporterOccupation));
        when(bailCase.read(SUPPORTER_4_IMMIGRATION, String.class)).thenReturn(Optional.of(supporterImmigration));
        when(bailCase.read(SUPPORTER_4_NATIONALITY)).thenReturn(Optional.of(supporterNationalities));
        when(bailCase.read(SUPPORTER_4_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_4_PASSPORT, String.class)).thenReturn(Optional.of(supporterHasPassport));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES, String.class)).thenReturn(Optional.of(financialAmountSupporterUndertakes));
    }
}

