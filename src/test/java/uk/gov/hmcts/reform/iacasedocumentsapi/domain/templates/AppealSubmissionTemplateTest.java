package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AppealSubmissionTemplateTest {

    private final String templateName = "APPEAL_SUBMISSION_TEMPLATE.docx";
    @Mock private StringProvider stringProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private Document applicationOutOfTimeDocument;

    private LocalDateTime createdDate = LocalDateTime.parse("2020-12-31T12:34:56");
    private String appealReferenceNumber = "RP/11111/2020";
    private String legalRepReferenceNumber = "OUR-REF";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String homeOfficeDecisionDate = "2020-12-23";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String appellantDateOfBirth = "1999-12-31";
    private String appellantAddressLine1 = "123 Some Street";
    private String appellantAddressLine2 = "Some Area";
    private String appellantAddressLine3 = "Some District";
    private String appellantAddressPostTown = "Some Town";
    private String appellantAddressCounty = "South";
    private String appellantAddressPostCode = "AB1 2CD";
    private String appellantAddressCountry = "Iceland";
    private String appealType = "revocationOfProtection";
    private String newMatters = "Some new matters";
    private String email = "someone@something.com";
    private String mobileNumber = "07987654321";
    private String appellantTitle = "Mr";
    private String homeOfficeDecisionReceivedDate = "2020-12-23";
    private String dateEntryClearanceDecision = "2020-12-19";
    private String gwfReference = "GWF1234567";
    private String oocAddress = "1 Park Street, South Road, Kenya, KY 2AF";

    private AddressUk addressUk = new AddressUk(
            appellantAddressLine1,
            appellantAddressLine2,
            appellantAddressLine3,
            appellantAddressPostTown,
            appellantAddressCounty,
            appellantAddressPostCode,
            appellantAddressCountry
    );

    private List<String> appealGroundsForDisplay = Arrays.asList(
            "protectionRefugeeConvention",
            "protectionHumanRights"
    );

    private List<IdValue<Map<String, String>>> appellantNationalities =
            Arrays.asList(
                    new IdValue<>("111", ImmutableMap.of("code", "IS")),
                    new IdValue<>("222", ImmutableMap.of("code", "FI"))
            );

    private List<IdValue<Map<String, String>>> otherAppeals =
            Arrays.asList(
                    new IdValue<>("333", ImmutableMap.of("value", "1234")),
                    new IdValue<>("444", ImmutableMap.of("value", "5678"))
            );

    private String outOfTimeExplanation = "someOutOfTimeExplanation";
    private String outOfTimeDocumentFileName = "someOutOfTimeDocument.pdf";

    private YesOrNo wantsEmail = YesOrNo.YES;

    private AppealSubmissionTemplate appealSubmissionTemplate;

    @BeforeEach
    void setUp() {

        appealSubmissionTemplate =
                new AppealSubmissionTemplate(
                        templateName,
                        stringProvider
                );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, appealSubmissionTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(email));
        when(asylumCase.read(APPELLANT_ADDRESS)).thenReturn(Optional.of(addressUk));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.of(newMatters));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(APPELLANT_TITLE, String.class)).thenReturn(Optional.of(appellantTitle));

        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(stringProvider.get("appealType", appealType)).thenReturn(Optional.of(appealType));


        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(appellantNationalities));
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.of(otherAppeals));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.of(outOfTimeExplanation));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.of(applicationOutOfTimeDocument));

        when(stringProvider.get("isoCountries", "IS")).thenReturn(Optional.of("Iceland"));
        when(stringProvider.get("isoCountries", "FI")).thenReturn(Optional.of("Finland"));

        when(stringProvider.get("appealGrounds", "protectionRefugeeConvention")).thenReturn(Optional.of("Refugee convention"));

        when(stringProvider.get("appealGrounds", "protectionHumanRights")).thenReturn(Optional.of("Human rights"));

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(27, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("31122020", templateFieldValues.get("CREATED_DATE"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("23122020", templateFieldValues.get("homeOfficeDecisionDate"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals("31121999", templateFieldValues.get("appellantDateOfBirth"));
        assertEquals(appealType, templateFieldValues.get("appealType"));
        assertEquals(newMatters, templateFieldValues.get("newMatters"));

        assertEquals(7, ((Map) templateFieldValues.get("appellantAddress")).size());
        assertEquals(appellantAddressLine1, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine1"));
        assertEquals(appellantAddressLine2, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine2"));
        assertEquals(appellantAddressLine3, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine3"));
        assertEquals(appellantAddressPostTown, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressPostTown"));
        assertEquals(appellantAddressCounty, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressCounty"));
        assertEquals(appellantAddressPostCode, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressPostCode"));
        assertEquals(appellantAddressCountry, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressCountry"));

        assertEquals(2, ((List) templateFieldValues.get("appellantNationalities")).size());
        assertEquals(ImmutableMap.of("nationality", "Iceland"), ((List) templateFieldValues.get("appellantNationalities")).get(0));
        assertEquals(ImmutableMap.of("nationality", "Finland"), ((List) templateFieldValues.get("appellantNationalities")).get(1));

        assertEquals(2, ((List) templateFieldValues.get("appealGrounds")).size());
        assertEquals(ImmutableMap.of("appealGround", "Refugee convention"), ((List) templateFieldValues.get("appealGrounds")).get(0));
        assertEquals(ImmutableMap.of("appealGround", "Human rights"), ((List) templateFieldValues.get("appealGrounds")).get(1));

        assertEquals("1234, 5678", templateFieldValues.get("otherAppeals"));

        assertEquals(outOfTimeExplanation, templateFieldValues.get("applicationOutOfTimeExplanation"));
        assertEquals(YesOrNo.YES, templateFieldValues.get("submissionOutOfTime"));
        assertEquals("", templateFieldValues.get("applicationOutOfTimeDocumentName"));

        assertEquals(wantsEmail, templateFieldValues.get("wantsEmail"));
        assertEquals(email, templateFieldValues.get("email"));
    }

    @Test
    void should_not_add_appeal_type_if_not_present() {

        dataSetUp();
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(24, templateFieldValues.size());
        assertFalse(templateFieldValues.containsKey("appealType"));
    }

    void dataSetUp() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.of(newMatters));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_TITLE, String.class)).thenReturn(Optional.of(appellantTitle));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mobileNumber));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(appellantNationalities));
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.of(otherAppeals));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.of(outOfTimeExplanation));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.of(applicationOutOfTimeDocument));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.empty());
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.empty());
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.empty());

    }

    @Test
    void should_add_out_of_country_and_no_sponsor() {

        dataSetUp();
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionReceivedDate));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(gwfReference));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION, String.class)).thenReturn(Optional.of(dateEntryClearanceDecision));
        when(asylumCase.read(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(29, templateFieldValues.size());
        assertTrue(templateFieldValues.containsKey("appealOutOfCountry"));
        assertTrue(templateFieldValues.containsKey("decisionLetterReceivedDate"));
        assertTrue(templateFieldValues.containsKey("outOfCountryDecisionType"));
        assertTrue(templateFieldValues.containsKey("decisionLetterReceived"));
        assertTrue(templateFieldValues.containsKey("gwfReferenceNumber"));
        assertTrue(templateFieldValues.containsKey("dateEntryClearanceDecision"));
        assertTrue(templateFieldValues.containsKey("decisionLetterReceived"));
        assertFalse(templateFieldValues.containsKey("appellantOutOfCountryAddress"));
    }

    @Test
    void should_add_out_of_country_and_with_sponsor() {

        dataSetUp();
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionReceivedDate));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(gwfReference));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION, String.class)).thenReturn(Optional.of(dateEntryClearanceDecision));
        when(asylumCase.read(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_OUT_OF_COUNTRY_ADDRESS, String.class)).thenReturn(Optional.ofNullable(oocAddress));


        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SPONSOR_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Spencer"));
        when(asylumCase.read(SPONSOR_FAMILY_NAME, String.class)).thenReturn(Optional.of("Jones"));
        when(asylumCase.read(SPONSOR_ADDRESS_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Address"));
        when(asylumCase.read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(SPONSOR_EMAIL, String.class)).thenReturn(Optional.of("sponsor@test.com"));

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(35, templateFieldValues.size());
        assertTrue(templateFieldValues.containsKey("appellantOutOfCountryAddress"));
        assertTrue(templateFieldValues.containsKey("hasSponsor"));
        assertTrue(templateFieldValues.containsKey("sponsorGivenNames"));
        assertTrue(templateFieldValues.containsKey("sponsorFamilyName"));
        assertTrue(templateFieldValues.containsKey("sponsorAddress"));
        assertTrue(templateFieldValues.containsKey("wantsSponsorEmail"));
        assertTrue(templateFieldValues.containsKey("sponsorEmail"));
        assertFalse(templateFieldValues.containsKey("sponsorMobileNumber"));
    }

    @Test
    void should_not_add_address_if_no_fixed_address_exists() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.of(newMatters));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_TITLE, String.class)).thenReturn(Optional.of(appellantTitle));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mobileNumber));

        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(appellantNationalities));
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.of(otherAppeals));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.of(outOfTimeExplanation));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.of(applicationOutOfTimeDocument));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.empty());
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.empty());
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(24, templateFieldValues.size());
        assertFalse(templateFieldValues.containsKey("appellantAddress"));
    }

    @Test
    void should_be_tolerant_of_missing_data() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.of(newMatters));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_TITLE, String.class)).thenReturn(Optional.of(appellantTitle));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mobileNumber));

        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(appellantNationalities));
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.of(otherAppeals));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.of(outOfTimeExplanation));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.of(applicationOutOfTimeDocument));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.empty());
        when(asylumCase.read(NEW_MATTERS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_GROUNDS_FOR_DISPLAY)).thenReturn(Optional.empty());
        when(asylumCase.read(OTHER_APPEALS)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(24, templateFieldValues.size());

        assertFalse(templateFieldValues.containsKey("appealType"));
        assertFalse(templateFieldValues.containsKey("appellantAddress"));

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("31122020", templateFieldValues.get("CREATED_DATE"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeDecisionDate"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("appellantDateOfBirth"));
        assertEquals("", templateFieldValues.get("newMatters"));
        assertEquals(0, ((List) templateFieldValues.get("appellantNationalities")).size());
        assertEquals(0, ((List) templateFieldValues.get("appealGrounds")).size());
        assertEquals("", templateFieldValues.get("otherAppeals"));
        assertEquals("", templateFieldValues.get("applicationOutOfTimeExplanation"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("submissionOutOfTime"));
        assertEquals("", templateFieldValues.get("applicationOutOfTimeDocumentName"));
        assertEquals("07987654321", templateFieldValues.get("mobileNumber"));
    }
}
