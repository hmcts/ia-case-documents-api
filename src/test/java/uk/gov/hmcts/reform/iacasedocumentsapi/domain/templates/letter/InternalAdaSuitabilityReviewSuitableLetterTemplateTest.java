package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalAdaSuitabilityReviewSuitableLetterTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String templateName = "TB-IAC-DEC-ENG-00003.docx";
    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1234";
    private final String internalAdaCustomerServicesEmailAddress = "example@email.com";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final LocalDate now = LocalDate.now();
    private final String listCaseHearingDate = "2023-06-30T00:00:00.000";
    private final String formattedListCaseHearingDate = "30062023";
    private final String hearingType = "Video hearing type response";
    private final String vulnerabilities = "Health issues response";
    private final String pastExperiences = "Past experiences response";
    private final String multimedia = "Multimedia response";
    private final String singleSexCourt = "Single-sex response";
    private final String inCamera = "Private hearing response";
    private final String otherHearingRequest = "Other response";
    private final Parties directionParties = Parties.RESPONDENT;

    private final IdValue<Direction> respondentReviewDirection = new IdValue<>(
        "1",
        new Direction(
            "HO Review Request Direction",
            directionParties,
            "2023-08-16",
            "2023-06-02",
            DirectionTag.RESPONDENT_REVIEW,
            Collections.emptyList(),
            Collections.emptyList(),
            "95e90870-2429-4660-b9c2-4111aff37304",
            "someDirectionType"
        )
    );

    private final IdValue<Direction> requestCaseBuildingDirection = new IdValue<>(
        "1",
        new Direction(
            "Case Building Request Direction",
            directionParties,
            "2023-08-02",
            "2023-06-05",
            DirectionTag.REQUEST_CASE_BUILDING,
            Collections.emptyList(),
            Collections.emptyList(),
            "95e90870-2429-4660-b9c2-4111aff45604",
            "someDirectionType"
        )
    );

    private InternalAdaSuitabilityReviewSuitableLetterTemplate internalAdaSuitabilityReviewSuitableLetterTemplate;

    @BeforeEach
    void setUp() {
        internalAdaSuitabilityReviewSuitableLetterTemplate =
            new InternalAdaSuitabilityReviewSuitableLetterTemplate(
                templateName,
                customerServicesProvider
            );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalAdaSuitabilityReviewSuitableLetterTemplate.getName());
    }

    void dataSetUp() {
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(respondentReviewDirection);
        directionList.add(requestCaseBuildingDirection);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingType));
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(vulnerabilities));
        when(asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(multimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(inCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(otherHearingRequest));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(internalAdaCustomerServicesEmailAddress);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalAdaSuitabilityReviewSuitableLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(18, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("ADAemail"));
        assertEquals(formattedListCaseHearingDate, templateFieldValues.get("hearingDate"));
        assertEquals(hearingType, templateFieldValues.get("hearingType"));
        assertEquals(vulnerabilities, templateFieldValues.get("vulnerabilities"));
        assertEquals(pastExperiences, templateFieldValues.get("pastExperiences"));
        assertEquals(multimedia, templateFieldValues.get("multimedia"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(inCamera, templateFieldValues.get("inCamera"));
        assertEquals(otherHearingRequest, templateFieldValues.get("otherHearingRequest"));
        assertEquals("2 Aug 2023", templateFieldValues.get("caseBuildingDueDate"));
        assertEquals("16 Aug 2023", templateFieldValues.get("requestRespondentReviewDueDate"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
    }
}
