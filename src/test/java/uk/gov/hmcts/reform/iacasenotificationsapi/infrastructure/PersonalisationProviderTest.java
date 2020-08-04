package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class PersonalisationProviderTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock AsylumCase asylumCase;
    @Mock AsylumCase asylumCaseBefore;
    @Mock HearingDetailsFinder hearingDetailsFinder;
    @Mock DateTimeExtractor dateTimeExtractor;
    @Mock Direction direction;
    @Mock DirectionFinder directionFinder;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private String iaExUiFrontendUrl = "http://localhost";

    private String hearingCentreAddress = "some hearing centre address";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepReferenceNumber = "legalRepReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";

    private String directionEditExplanation = "This is edit direction explanation";
    private String directionEditDateDue = "2020-02-14";

    private String oldHearingCentreName = HearingCentre.MANCHESTER.toString();
    private String oldHearingDateTime = "2019-08-20T14:25:15.000";
    private String oldHearingDate = "2019-08-20";

    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsOther = "someRequirementsOther";

    private String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private String directionExplanation = "someExplanation";
    private String directionDueDate = "2019-10-29";

    private String applicantGrantedTemplateId = "applicantGrantedTemplateId";
    private String applicantPartiallyGrantedTemplateId = "applicantPartiallyGrantedTemplateId";
    private String applicantNotAdmittedTemplateId = "applicantNotAdmittedTemplateId";
    private String applicantRefusedTemplateId = "applicantRefusedTemplateId";

    private String otherPartyGrantedTemplateId = "otherPartyGrantedTemplateId";
    private String otherPartyPartiallyGrantedTemplateId = "otherPartyPartiallyGrantedTemplateId";
    private String otherPartyNotAdmittedTemplateId = "otherPartyNotAdmittedTemplateId";
    private String otherPartyRefusedTemplateId = "otherPartyRefusedTemplateId";

    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;

    private YesOrNo yes = YesOrNo.YES;
    private YesOrNo no = YesOrNo.NO;

    private PersonalisationProvider personalisationProvider;

    @Before
    public void setUp() {
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCaseBefore)).thenReturn(oldHearingCentreName);
        when(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore)).thenReturn(oldHearingDateTime);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingDate(oldHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedOther));
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
    public void should_return_edit_case_listing_personalisation() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertThat(personalisation.get("hearingRequirementVulnerabilities")).isEqualTo(requirementsVulnerabilities);
        assertThat(personalisation.get("hearingRequirementMultimedia")).isEqualTo(requirementsMultimedia);
        assertThat(personalisation.get("hearingRequirementSingleSexCourt")).isEqualTo(requirementsSingleSexCourt);
        assertThat(personalisation.get("hearingRequirementInCameraCourt")).isEqualTo(requirementsInCamera);
        assertThat(personalisation.get("hearingRequirementOther")).isEqualTo(requirementsOther);
    }

    @Test
    public void should_return_edit_case_listing_personalisation_when_submit_hearing_present() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertThat(personalisation.get("hearingRequirementVulnerabilities")).isEqualTo(caseOfficerReviewedVulnerabilities);
        assertThat(personalisation.get("hearingRequirementMultimedia")).isEqualTo(caseOfficerReviewedMultimedia);
        assertThat(personalisation.get("hearingRequirementSingleSexCourt")).isEqualTo(caseOfficerReviewedSingleSexCourt);
        assertThat(personalisation.get("hearingRequirementInCameraCourt")).isEqualTo(caseOfficerReviewedInCamera);
        assertThat(personalisation.get("hearingRequirementOther")).isEqualTo(caseOfficerReviewedOther);
    }

    @Test
    public void should_return_uploaded_additional_evidence_personalisation() {
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_non_direction_personalisation() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation = personalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_change_direction_due_date_personalisation() {
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.of(directionEditExplanation));
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(directionEditDateDue));

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_legal_rep_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
    }

    @Test
    public void should_return_home_office_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }
}
