package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.AGREES_TO_BOUND_BY_FINANCIAL_COND;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_BEEN_REFUSED_BAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETAINED_LOC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY1;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GENDER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.GROUNDS_FOR_BAIL_REASONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HAS_PREVIOUS_BAIL_APPLICATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_YES_NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SENT_BY_CHECKLIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_HAS_PASSPORT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_IMMIGRATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_NATIONALITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_OCCUPATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_PASSPORT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_RELATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_YES_OR_NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING1;

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
    public void should_set_sentBy_properly_for_admin() {
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
    public void should_not_set_previous_application_reference_if_not_present() {
        dataSetUp();
        when(bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class)).thenReturn(Optional.of("No"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("previousBailApplicationNumber"));
    }

    @Test
    public void should_not_set_previous_appeal_reference_if_not_present() {
        dataSetUp();
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class)).thenReturn(Optional.of("No"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        assertFalse(fieldValuesMap.containsKey("appealReferenceNumber"));
    }

    @Test
    public void should_set_disability_details_if_present() {
        dataSetUp();
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields(fieldValuesMap);
        assertTrue(fieldValuesMap.containsKey("applicantDisabilityDetails"));
    }

    @Test
    public void should_set_prison_details_if_detained_in_prison() {
        dataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields(fieldValuesMap);
        assertTrue(fieldValuesMap.containsKey("prisonName"));
        assertTrue(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertFalse(fieldValuesMap.containsKey("ircName"));
    }

    @Test
    public void should_set_prison_details_if_detained_in_irc() {
        dataSetUp();
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Derwentside"));
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields(fieldValuesMap);
        assertFalse(fieldValuesMap.containsKey("prisonName"));
        assertFalse(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertTrue(fieldValuesMap.containsKey("ircName"));
    }

    @Test
    public void should_not_set_supporter_2_data_if_not_present() {
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
    public void should_be_tolerant_to_empty_data() {
        dataSetUp();
        when(bailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_PHONE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields(fieldValuesMap);
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
    public void should_set_the_fields_for_application_submittedBy_LR() {
        dataSetUp();
        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);

        checkCommonFields(fieldValuesMap);
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
    public void should_set_the_fields_for_application_submittedBy_HO() {
        dataSetUp();
        when(bailCase.read(IS_HOME_OFFICE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        fieldValuesMap = bailSubmissionTemplate.mapFieldValues(caseDetails);
        checkCommonFields(fieldValuesMap);
        assertFalse(fieldValuesMap.containsKey("legalRepCompany"));
        assertFalse(fieldValuesMap.containsKey("legalRepName"));
        assertFalse(fieldValuesMap.containsKey("legalRepEmail"));
        assertFalse(fieldValuesMap.containsKey("legalRepPhone"));
        assertFalse(fieldValuesMap.containsKey("legalRepReference"));
    }

    //Helper method for common assertions
    private void checkCommonFields(Map<String, Object> fieldValuesMap) {
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

}
