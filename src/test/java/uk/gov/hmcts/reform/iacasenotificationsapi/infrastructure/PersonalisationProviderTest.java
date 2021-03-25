package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersonalisationProviderTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    AsylumCase asylumCase;
    @Mock
    AsylumCase asylumCaseBefore;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    Direction direction;
    @Mock
    DirectionFinder directionFinder;

    private final String iaExUiFrontendUrl = "http://localhost";

    private final String hearingCentreName = HearingCentre.TAYLOR_HOUSE.toString();
    private final String hearingCentreAddress = "some hearing centre address";

    private final String hearingDateTime = "2019-08-27T14:25:15.000";
    private final String hearingDate = "2019-08-27";
    private final String hearingTime = "14:25";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "legalRepReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "appellantGivenNames";
    private final String appellantFamilyName = "appellantFamilyName";
    private final String homeOfficeRefNumber = "homeOfficeRefNumber";

    private final String directionEditExplanation = "This is edit direction explanation";
    private final String directionEditDateDue = "2020-02-14";

    private final String oldHearingCentreName = HearingCentre.MANCHESTER.toString();
    private final String oldHearingDateTime = "2019-08-20T14:25:15.000";
    private final String oldHearingDate = "2019-08-20";

    private final String remoteVideoCallTribunalResponse = "someRemoteVideoCallTribunalResponse";

    private final String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private final String requirementsMultimedia = "someRequirementsMultimedia";
    private final String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private final String requirementsInCamera = "someRequirementsInCamera";
    private final String requirementsOther = "someRequirementsOther";

    private final String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private final String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private final String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private final String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private final String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private final String directionExplanation = "someExplanation";
    private final String directionDueDate = "2019-10-29";

    private PersonalisationProvider personalisationProvider;

    @BeforeEach
    public void setUp() {
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentreName);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCaseBefore)).thenReturn(oldHearingCentreName);
        when(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore)).thenReturn(oldHearingDateTime);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingDate(oldHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(remoteVideoCallTribunalResponse));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedOther));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));
        when(direction.getDateDue()).thenReturn(directionDueDate);
        when(direction.getExplanation()).thenReturn(directionExplanation);

        personalisationProvider = new PersonalisationProvider(
            iaExUiFrontendUrl,
            hearingDetailsFinder,
            directionFinder,
            dateTimeExtractor
        );
    }

    @Test
    void should_return_edit_case_listing_personalisation() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertThat(personalisation.get("remoteVideoCallTribunalResponse")).contains(remoteVideoCallTribunalResponse);
        assertThat(personalisation.get("hearingRequirementVulnerabilities")).contains(requirementsVulnerabilities);
        assertThat(personalisation.get("hearingRequirementMultimedia")).contains(requirementsMultimedia);
        assertThat(personalisation.get("hearingRequirementSingleSexCourt")).contains(requirementsSingleSexCourt);
        assertThat(personalisation.get("hearingRequirementInCameraCourt")).contains(requirementsInCamera);
        assertThat(personalisation.get("hearingRequirementOther")).contains(requirementsOther);
    }

    @Test
    void should_return_edit_case_listing_personalisation_when_submit_hearing_present() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertThat(personalisation.get("remoteVideoCallTribunalResponse"))
            .contains(remoteVideoCallTribunalResponse);
        assertThat(personalisation.get("hearingRequirementVulnerabilities"))
            .contains(caseOfficerReviewedVulnerabilities);
        assertThat(personalisation.get("hearingRequirementMultimedia")).contains(caseOfficerReviewedMultimedia);
        assertThat(personalisation.get("hearingRequirementSingleSexCourt"))
            .contains(caseOfficerReviewedSingleSexCourt);
        assertThat(personalisation.get("hearingRequirementInCameraCourt")).contains(caseOfficerReviewedInCamera);
        assertThat(personalisation.get("hearingRequirementOther")).contains(caseOfficerReviewedOther);
    }

    @Test
    void should_return_uploaded_additional_evidence_personalisation() {
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_non_direction_personalisation() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            personalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_change_direction_due_date_personalisation() {
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class))
            .thenReturn(Optional.of(directionEditExplanation));
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(directionEditDateDue));

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_legal_rep_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
    }

    @Test
    void should_return_home_office_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }
}
