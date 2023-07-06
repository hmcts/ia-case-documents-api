package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.*;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingRequirementsTemplateTest {

    private final String templateName = "HEARING_REQUIREMENTS_TEMPLATE.docx";

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private String appealReferenceNumber = "RP/11111/2020";
    private String legalRepReferenceNumber = "OUR-REF";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private YesOrNo isAppellantAttendingTheHearing = YesOrNo.YES;
    private YesOrNo isAppellantGivingOralEvidence = YesOrNo.YES;

    private YesOrNo isWitnessesAttending = YesOrNo.YES;
    private WitnessDetails witness1 = new WitnessDetails();
    private WitnessDetails witness2 = new WitnessDetails();

    private YesOrNo isInterpreterServicesNeeded = YesOrNo.YES;
    private InterpreterLanguage interpreter1 = new InterpreterLanguage();
    private InterpreterLanguage interpreter2 = new InterpreterLanguage();

    private YesOrNo isHearingRoomNeeded = YesOrNo.YES;
    private YesOrNo isHearingLoopNeeded = YesOrNo.YES;

    private YesOrNo physicalOrMentalHealthIssues = YesOrNo.YES;
    private String physicalOrMentalHealthIssuesDescription = "Physical issues description";

    private YesOrNo pastExperiences = YesOrNo.YES;
    private String pastExperiencesDescription = "Past experiences description";

    private YesOrNo isOutOfCountryEnabled = YesOrNo.YES;
    private YesOrNo remoteVideoCall = YesOrNo.YES;
    private String remoteVideoCallDescription = "Remote video call evidence description";

    private YesOrNo multimediaEvidence = YesOrNo.YES;
    private String multimediaEvidenceDescription = "Multimedia evidence description";

    private YesOrNo singleSexCourt = YesOrNo.YES;
    private MaleOrFemale singleSexCourtType = MaleOrFemale.FEMALE;
    private String singleSexCourtTypeDescription = "Single-sex court type description";

    private YesOrNo inCameraCourt = YesOrNo.YES;
    private String inCameraCourtDescription = "In camera court description";

    private YesOrNo additionalRequests = YesOrNo.YES;
    private String additionalRequestsDescription = "Additional requests description";

    private YesOrNo datesToAvoid = YesOrNo.YES;
    private DatesToAvoid datesToAvoid1 = new DatesToAvoid();
    private DatesToAvoid datesToAvoid2 = new DatesToAvoid();

    private List<IdValue<WitnessDetails>> witnessDetails;
    private List<IdValue<InterpreterLanguage>> interpreterLanguage;
    private List<IdValue<DatesToAvoid>> datesToAvoidList;

    private HearingRequirementsTemplate hearingRequirementsTemplate;

    @BeforeEach
    public void setUp() {

        hearingRequirementsTemplate =
            new HearingRequirementsTemplate(
                templateName
            );

        witness1.setWitnessName("Some Witness");
        witness1.setWitnessFamilyName("Some Witness Family");
        witness2.setWitnessName("Another Witness");

        witnessDetails =
            Arrays.asList(
                new IdValue<>("111", witness1),
                new IdValue<>("222", witness2));

        interpreter1.setLanguage("Nepali");
        interpreter1.setLanguageDialect("Dialect A");
        interpreter2.setLanguage("Serbian");
        interpreter2.setLanguageDialect("Dialect B");

        interpreterLanguage =
            Arrays.asList(
                new IdValue<>("111", interpreter1),
                new IdValue<>("222", interpreter2)
            );

        datesToAvoid1.setDateToAvoid(LocalDate.parse("2019-12-25"));
        datesToAvoid2.setDateToAvoid(LocalDate.parse("2020-01-01"));
        datesToAvoid1.setDateToAvoidReason("Christmas");
        datesToAvoid2.setDateToAvoidReason("New Year");

        datesToAvoidList =
            Arrays.asList(
                new IdValue<>("111", datesToAvoid1),
                new IdValue<>("222", datesToAvoid2)
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, hearingRequirementsTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values_for_in_country_appeal() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isAppellantAttendingTheHearing));
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(isAppellantGivingOralEvidence));

        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(isWitnessesAttending));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isInterpreterServicesNeeded));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingRoomNeeded));
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingLoopNeeded));

        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isOutOfCountryEnabled));
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(remoteVideoCall));
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.of(remoteVideoCallDescription));

        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.of(physicalOrMentalHealthIssues));
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of(physicalOrMentalHealthIssuesDescription));

        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.of(pastExperiencesDescription));

        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(multimediaEvidence));
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.of(multimediaEvidenceDescription));

        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.of(singleSexCourtType));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.of(singleSexCourtTypeDescription));

        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(inCameraCourt));
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.of(inCameraCourtDescription));

        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.of(additionalRequests));
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.of(additionalRequestsDescription));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(datesToAvoid));

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList));

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(37, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("appealOutOfCountry"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkOoc"));
        assertEquals(isAppellantAttendingTheHearing, templateFieldValues.get("isAppellantAttendingTheHearing"));
        assertEquals(isAppellantGivingOralEvidence, templateFieldValues.get("isAppellantGivingOralEvidence"));
        assertEquals(isWitnessesAttending, templateFieldValues.get("isWitnessesAttending"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkInCountry"));
        assertEquals(2, ((List) templateFieldValues.get("witnessDetails")).size());
        assertEquals(ImmutableMap.of("witnessDetails", "Some Witness Some Witness Family"), ((List) templateFieldValues.get("witnessDetails")).get(0));
        assertEquals(ImmutableMap.of("witnessDetails", "Another Witness"), ((List) templateFieldValues.get("witnessDetails")).get(1));
        assertEquals(isInterpreterServicesNeeded, templateFieldValues.get("isInterpreterServicesNeeded"));
        assertEquals(2, ((List) templateFieldValues.get("language")).size());
        assertEquals(ImmutableMap.of("language", "Nepali"), ((List) templateFieldValues.get("language")).get(0));
        assertEquals(ImmutableMap.of("language", "Serbian"), ((List) templateFieldValues.get("language")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("languageDialect")).size());
        assertEquals(ImmutableMap.of("languageDialect", "Dialect A"), ((List) templateFieldValues.get("languageDialect")).get(0));
        assertEquals(ImmutableMap.of("languageDialect", "Dialect B"), ((List) templateFieldValues.get("languageDialect")).get(1));
        assertEquals(isHearingRoomNeeded, templateFieldValues.get("isHearingRoomNeeded"));
        assertEquals(isHearingLoopNeeded, templateFieldValues.get("isHearingLoopNeeded"));
        assertEquals(remoteVideoCall, templateFieldValues.get("remoteVideoCall"));
        assertEquals(remoteVideoCallDescription, templateFieldValues.get("remoteVideoCallDescription"));
        assertEquals(physicalOrMentalHealthIssues, templateFieldValues.get("physicalOrMentalHealthIssues"));
        assertEquals(physicalOrMentalHealthIssuesDescription, templateFieldValues.get("physicalOrMentalHealthIssuesDescription"));
        assertEquals(pastExperiences, templateFieldValues.get("pastExperiences"));
        assertEquals(pastExperiencesDescription, templateFieldValues.get("pastExperiencesDescription"));
        assertEquals(multimediaEvidence, templateFieldValues.get("multimediaEvidence"));
        assertEquals(multimediaEvidenceDescription, templateFieldValues.get("multimediaEvidenceDescription"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(singleSexCourtType, templateFieldValues.get("singleSexCourtType"));
        assertEquals(singleSexCourtTypeDescription, templateFieldValues.get("singleSexCourtTypeDescription"));
        assertEquals(inCameraCourt, templateFieldValues.get("inCameraCourt"));
        assertEquals(inCameraCourtDescription, templateFieldValues.get("inCameraCourtDescription"));
        assertEquals(additionalRequests, templateFieldValues.get("additionalRequests"));
        assertEquals(additionalRequestsDescription, templateFieldValues.get("additionalRequestsDescription"));
        assertEquals(datesToAvoid, templateFieldValues.get("datesToAvoid"));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 2020"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", "New Year"), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));
    }

    @Test
    public void should_map_case_data_to_template_field_values_for_out_of_country_appeal() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isAppellantAttendingTheHearing));
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(isAppellantGivingOralEvidence));

        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(isWitnessesAttending));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isInterpreterServicesNeeded));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingRoomNeeded));
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingLoopNeeded));

        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.of(physicalOrMentalHealthIssues));
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of(physicalOrMentalHealthIssuesDescription));

        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.of(pastExperiencesDescription));

        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isOutOfCountryEnabled));
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(remoteVideoCall));
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.of(remoteVideoCallDescription));

        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(multimediaEvidence));
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.of(multimediaEvidenceDescription));

        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.of(singleSexCourtType));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.of(singleSexCourtTypeDescription));

        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(inCameraCourt));
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.of(inCameraCourtDescription));

        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.of(additionalRequests));
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.of(additionalRequestsDescription));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(datesToAvoid));

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList));

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(37, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(YesOrNo.YES, templateFieldValues.get("appealOutOfCountry"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkOoc"));
        assertEquals(isAppellantAttendingTheHearing, templateFieldValues.get("isAppellantAttendingTheHearing"));
        assertEquals(isAppellantGivingOralEvidence, templateFieldValues.get("isAppellantGivingOralEvidence"));
        assertEquals(isWitnessesAttending, templateFieldValues.get("isWitnessesAttending"));
        assertEquals(2, ((List) templateFieldValues.get("witnessDetails")).size());
        assertEquals(ImmutableMap.of("witnessDetails", "Some Witness Some Witness Family"), ((List) templateFieldValues.get("witnessDetails")).get(0));
        assertEquals(ImmutableMap.of("witnessDetails", "Another Witness"), ((List) templateFieldValues.get("witnessDetails")).get(1));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkInCountry"));
        assertEquals(isInterpreterServicesNeeded, templateFieldValues.get("isInterpreterServicesNeeded"));
        assertEquals(2, ((List) templateFieldValues.get("language")).size());
        assertEquals(ImmutableMap.of("language", "Nepali"), ((List) templateFieldValues.get("language")).get(0));
        assertEquals(ImmutableMap.of("language", "Serbian"), ((List) templateFieldValues.get("language")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("languageDialect")).size());
        assertEquals(ImmutableMap.of("languageDialect", "Dialect A"), ((List) templateFieldValues.get("languageDialect")).get(0));
        assertEquals(ImmutableMap.of("languageDialect", "Dialect B"), ((List) templateFieldValues.get("languageDialect")).get(1));
        assertEquals(isHearingRoomNeeded, templateFieldValues.get("isHearingRoomNeeded"));
        assertEquals(isHearingLoopNeeded, templateFieldValues.get("isHearingLoopNeeded"));
        assertEquals(isOutOfCountryEnabled, templateFieldValues.get("isOutOfCountryEnabled"));
        assertEquals(remoteVideoCall, templateFieldValues.get("remoteVideoCall"));
        assertEquals(remoteVideoCallDescription, templateFieldValues.get("remoteVideoCallDescription"));
        assertEquals(physicalOrMentalHealthIssues, templateFieldValues.get("physicalOrMentalHealthIssues"));
        assertEquals(physicalOrMentalHealthIssuesDescription, templateFieldValues.get("physicalOrMentalHealthIssuesDescription"));
        assertEquals(pastExperiences, templateFieldValues.get("pastExperiences"));
        assertEquals(pastExperiencesDescription, templateFieldValues.get("pastExperiencesDescription"));
        assertEquals(multimediaEvidence, templateFieldValues.get("multimediaEvidence"));
        assertEquals(multimediaEvidenceDescription, templateFieldValues.get("multimediaEvidenceDescription"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(singleSexCourtType, templateFieldValues.get("singleSexCourtType"));
        assertEquals(singleSexCourtTypeDescription, templateFieldValues.get("singleSexCourtTypeDescription"));
        assertEquals(inCameraCourt, templateFieldValues.get("inCameraCourt"));
        assertEquals(inCameraCourtDescription, templateFieldValues.get("inCameraCourtDescription"));
        assertEquals(additionalRequests, templateFieldValues.get("additionalRequests"));
        assertEquals(additionalRequestsDescription, templateFieldValues.get("additionalRequestsDescription"));
        assertEquals(datesToAvoid, templateFieldValues.get("datesToAvoid"));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 2020"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", "New Year"), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));

        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(37, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(isAppellantAttendingTheHearing, templateFieldValues.get("isAppellantAttendingTheHearing"));
        assertEquals(isAppellantGivingOralEvidence, templateFieldValues.get("isAppellantGivingOralEvidence"));
        assertEquals(isWitnessesAttending, templateFieldValues.get("isWitnessesAttending"));
        assertEquals(2, ((List) templateFieldValues.get("witnessDetails")).size());
        assertEquals(ImmutableMap.of("witnessDetails", "Some Witness Some Witness Family"), ((List) templateFieldValues.get("witnessDetails")).get(0));
        assertEquals(ImmutableMap.of("witnessDetails", "Another Witness"), ((List) templateFieldValues.get("witnessDetails")).get(1));
        assertEquals(isInterpreterServicesNeeded, templateFieldValues.get("isInterpreterServicesNeeded"));
        assertEquals(2, ((List) templateFieldValues.get("language")).size());
        assertEquals(ImmutableMap.of("language", "Nepali"), ((List) templateFieldValues.get("language")).get(0));
        assertEquals(ImmutableMap.of("language", "Serbian"), ((List) templateFieldValues.get("language")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("languageDialect")).size());
        assertEquals(ImmutableMap.of("languageDialect", "Dialect A"), ((List) templateFieldValues.get("languageDialect")).get(0));
        assertEquals(ImmutableMap.of("languageDialect", "Dialect B"), ((List) templateFieldValues.get("languageDialect")).get(1));
        assertEquals(isHearingRoomNeeded, templateFieldValues.get("isHearingRoomNeeded"));
        assertEquals(isHearingLoopNeeded, templateFieldValues.get("isHearingLoopNeeded"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("remoteVideoCall"));
        assertEquals(physicalOrMentalHealthIssues, templateFieldValues.get("physicalOrMentalHealthIssues"));
        assertEquals(physicalOrMentalHealthIssuesDescription, templateFieldValues.get("physicalOrMentalHealthIssuesDescription"));
        assertEquals(pastExperiences, templateFieldValues.get("pastExperiences"));
        assertEquals(pastExperiencesDescription, templateFieldValues.get("pastExperiencesDescription"));
        assertEquals(isOutOfCountryEnabled, templateFieldValues.get("isOutOfCountryEnabled"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("remoteVideoCall"));
        assertEquals(remoteVideoCallDescription, templateFieldValues.get("remoteVideoCallDescription"));
        assertEquals(multimediaEvidence, templateFieldValues.get("multimediaEvidence"));
        assertEquals(multimediaEvidenceDescription, templateFieldValues.get("multimediaEvidenceDescription"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(singleSexCourtType, templateFieldValues.get("singleSexCourtType"));
        assertEquals(singleSexCourtTypeDescription, templateFieldValues.get("singleSexCourtTypeDescription"));
        assertEquals(inCameraCourt, templateFieldValues.get("inCameraCourt"));
        assertEquals(inCameraCourtDescription, templateFieldValues.get("inCameraCourtDescription"));
        assertEquals(additionalRequests, templateFieldValues.get("additionalRequests"));
        assertEquals(additionalRequestsDescription, templateFieldValues.get("additionalRequestsDescription"));
        assertEquals(datesToAvoid, templateFieldValues.get("datesToAvoid"));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 2020"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", "New Year"), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));
    }

    @Test
    void should_map_case_data_to_template_field_values_no_dates_to_avoid_flag() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isAppellantAttendingTheHearing));
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(isAppellantGivingOralEvidence));

        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(isWitnessesAttending));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isInterpreterServicesNeeded));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingRoomNeeded));
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingLoopNeeded));

        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.of(physicalOrMentalHealthIssues));
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of(physicalOrMentalHealthIssuesDescription));

        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.of(pastExperiencesDescription));

        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isOutOfCountryEnabled));
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(remoteVideoCall));
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.of(remoteVideoCallDescription));

        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(multimediaEvidence));
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.of(multimediaEvidenceDescription));

        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.of(singleSexCourtType));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.of(singleSexCourtTypeDescription));

        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(inCameraCourt));
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.of(inCameraCourtDescription));

        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.of(additionalRequests));
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.of(additionalRequestsDescription));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(datesToAvoid));

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(YesOrNo.YES, templateFieldValues.get("datesToAvoid"));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 2020"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", "New Year"), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));
    }

    @Test
    void should_be_tolerant_of_missing_data() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.empty());

        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(37, templateFieldValues.size());

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));

        assertEquals(YesOrNo.NO, templateFieldValues.get("appealOutOfCountry"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkOoc"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isEvidenceFromOutsideUkInCountry"));

        assertEquals(YesOrNo.NO, templateFieldValues.get("isAppellantAttendingTheHearing"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isAppellantGivingOralEvidence"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isWitnessesAttending"));
        assertEquals(0, ((List) templateFieldValues.get("witnessDetails")).size());
        assertEquals(YesOrNo.NO, templateFieldValues.get("isWitnessesAttending"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isInterpreterServicesNeeded"));
        assertEquals(0, ((List) templateFieldValues.get("language")).size());
        assertEquals(0, ((List) templateFieldValues.get("languageDialect")).size());
        assertEquals(YesOrNo.NO, templateFieldValues.get("isHearingRoomNeeded"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isHearingLoopNeeded"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("physicalOrMentalHealthIssues"));
        assertEquals("", templateFieldValues.get("physicalOrMentalHealthIssuesDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("pastExperiences"));
        assertEquals("", templateFieldValues.get("pastExperiencesDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isOutOfCountryEnabled"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("remoteVideoCall"));
        assertEquals("", templateFieldValues.get("remoteVideoCallDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("multimediaEvidence"));
        assertEquals("", templateFieldValues.get("multimediaEvidenceDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("singleSexCourt"));
        assertEquals(MaleOrFemale.NONE, templateFieldValues.get("singleSexCourtType"));
        assertEquals("", templateFieldValues.get("singleSexCourtTypeDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("inCameraCourt"));
        assertEquals("", templateFieldValues.get("inCameraCourtDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("additionalRequests"));
        assertEquals("", templateFieldValues.get("additionalRequestsDescription"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("datesToAvoid"));
        assertEquals(0, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(0, ((List) templateFieldValues.get("dateToAvoidReason")).size());
    }

    @Test
    void should_default_date_to_avoid_reason_null_values_to_empty_string() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isAppellantAttendingTheHearing));
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(isAppellantGivingOralEvidence));

        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(isWitnessesAttending));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isInterpreterServicesNeeded));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingRoomNeeded));
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingLoopNeeded));

        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.of(physicalOrMentalHealthIssues));
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of(physicalOrMentalHealthIssuesDescription));

        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.of(pastExperiencesDescription));

        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isOutOfCountryEnabled));
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(remoteVideoCall));
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.of(remoteVideoCallDescription));

        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(multimediaEvidence));
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.of(multimediaEvidenceDescription));

        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.of(singleSexCourtType));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.of(singleSexCourtTypeDescription));

        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(inCameraCourt));
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.of(inCameraCourtDescription));

        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.of(additionalRequests));
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.of(additionalRequestsDescription));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(datesToAvoid));

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList));

        datesToAvoid1.setDateToAvoid(LocalDate.parse("2019-12-25"));
        datesToAvoid2.setDateToAvoid(LocalDate.parse("2020-01-01"));

        datesToAvoid1.setDateToAvoidReason("Christmas");
        datesToAvoid2.setDateToAvoidReason(null);

        List<IdValue<DatesToAvoid>> datesToAvoidList2 =
            Arrays.asList(
                new IdValue<>("111", datesToAvoid1),
                new IdValue<>("222", datesToAvoid2)
            );

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList2));

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        //assertEquals(ImmutableMap.of("dateToAvoid", LocalDate.parse("2019-12-25")), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 2020"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", ""), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));
    }

    @Test
    void should_default_date_to_avoid_null_values_to_past_date() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_APPELLANT_ATTENDING_THE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isAppellantAttendingTheHearing));
        when(asylumCase.read(IS_APPELLANT_GIVING_ORAL_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(isAppellantGivingOralEvidence));

        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(isWitnessesAttending));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isInterpreterServicesNeeded));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingRoomNeeded));
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(isHearingLoopNeeded));

        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES, YesOrNo.class)).thenReturn(Optional.of(physicalOrMentalHealthIssues));
        when(asylumCase.read(PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of(physicalOrMentalHealthIssuesDescription));

        when(asylumCase.read(PAST_EXPERIENCES, YesOrNo.class)).thenReturn(Optional.of(pastExperiences));
        when(asylumCase.read(PAST_EXPERIENCES_DESCRIPTION, String.class)).thenReturn(Optional.of(pastExperiencesDescription));

        when(asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isOutOfCountryEnabled));
        when(asylumCase.read(REMOTE_VIDEO_CALL, YesOrNo.class)).thenReturn(Optional.of(remoteVideoCall));
        when(asylumCase.read(REMOTE_VIDEO_CALL_DESCRIPTION, String.class)).thenReturn(Optional.of(remoteVideoCallDescription));

        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(multimediaEvidence));
        when(asylumCase.read(MULTIMEDIA_EVIDENCE_DESCRIPTION, String.class)).thenReturn(Optional.of(multimediaEvidenceDescription));

        when(asylumCase.read(SINGLE_SEX_COURT, YesOrNo.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, MaleOrFemale.class)).thenReturn(Optional.of(singleSexCourtType));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE_DESCRIPTION, String.class)).thenReturn(Optional.of(singleSexCourtTypeDescription));

        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(inCameraCourt));
        when(asylumCase.read(IN_CAMERA_COURT_DESCRIPTION, String.class)).thenReturn(Optional.of(inCameraCourtDescription));

        when(asylumCase.read(ADDITIONAL_REQUESTS, YesOrNo.class)).thenReturn(Optional.of(additionalRequests));
        when(asylumCase.read(ADDITIONAL_REQUESTS_DESCRIPTION, String.class)).thenReturn(Optional.of(additionalRequestsDescription));

        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(datesToAvoid));

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList));

        datesToAvoid1.setDateToAvoid(LocalDate.parse("2019-12-25"));
        datesToAvoid2.setDateToAvoid(null);
        datesToAvoid1.setDateToAvoidReason("Christmas");
        datesToAvoid2.setDateToAvoidReason("New Year");

        List<IdValue<DatesToAvoid>> datesToAvoidList2 =
            Arrays.asList(
                new IdValue<>("111", datesToAvoid1),
                new IdValue<>("222", datesToAvoid2)
            );

        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoidList2));

        Map<String, Object> templateFieldValues = hearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(2, ((List) templateFieldValues.get("dateToAvoid")).size());
        assertEquals(ImmutableMap.of("dateToAvoid", "25 Dec 2019"), ((List) templateFieldValues.get("dateToAvoid")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoid", "1 Jan 1900"), ((List) templateFieldValues.get("dateToAvoid")).get(1));
        assertEquals(2, ((List) templateFieldValues.get("dateToAvoidReason")).size());
        assertEquals(ImmutableMap.of("dateToAvoidReason", "Christmas"), ((List) templateFieldValues.get("dateToAvoidReason")).get(0));
        assertEquals(ImmutableMap.of("dateToAvoidReason", "New Year"), ((List) templateFieldValues.get("dateToAvoidReason")).get(1));
    }
}
