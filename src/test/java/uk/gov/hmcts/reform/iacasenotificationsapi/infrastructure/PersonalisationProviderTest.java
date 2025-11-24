package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
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

    private final String caseOfficerReviewedVulnerabilitiesDisplay = "someCaseOfficerReviewedVulnerabilitiesDisplay";
    private final String caseOfficerReviewedMultimediaDisplay = "someCaseOfficerReviewedMultimediaDisplay";
    private final String caseOfficerReviewedSingleSexCourtDisplay = "someCaseOfficerReviewedSingleSexCourtDisplay";
    private final String caseOfficerReviewedInCameraDisplay = "someCaseOfficerReviewedInCameraDisplay";
    private final String caseOfficerReviewedOtherDisplay = "someCaseOfficerReviewedOtherDisplay";
    private final String caseOfficerReviewedRemoteHearingDisplay = "caseOfficerReviewedRemoteHearingDisplay";


    private final String directionExplanation = "someExplanation";
    private final String directionDueDate = "2019-10-29";
    private final String recipientReferenceNumber = "recipientReferenceNumber";
    private final String recipient = "recipient";
    private final String applyForCostsCreationDate = "2023-11-24";

    private final String applyForCostsDecision = "Order made";

    private static String homeOffice = "Home office";

    private PersonalisationProvider personalisationProvider;

    @BeforeEach
    public void setUp() {
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentreName);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getOldHearingCentreName(asylumCaseBefore)).thenReturn(oldHearingCentreName);
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(oldHearingDateTime));

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

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "false, true",
        "false, false"
    })
    void should_return_edit_case_listing_personalisation_when_submit_hearing_present(boolean displayFieldsPresent,
                                                                                     boolean responseFieldsPresent) {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedVulnerabilities) : Optional.empty());
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedMultimedia) : Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedSingleSexCourt) : Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedInCamera) : Optional.empty());
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedOther) : Optional.empty());
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(remoteVideoCallTribunalResponse) : Optional.empty());

        when(asylumCase.read(VULNERABILITIES_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedVulnerabilitiesDisplay) : Optional.empty());
        when(asylumCase.read(MULTIMEDIA_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedMultimediaDisplay) : Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedSingleSexCourtDisplay) : Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedInCameraDisplay) : Optional.empty());
        when(asylumCase.read(OTHER_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedOtherDisplay) : Optional.empty());

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        String vulnerabilitiesDefaultText = "No special adjustments are being made to accommodate vulnerabilities";
        String multimediaDefaultText = "No multimedia equipment is being provided";
        String singleSexCourtDefaultText = "The court will not be single sex";
        String inCameraCourtDefaultText = "The hearing will be held in public court";
        String otherAdjustmentsDefaultText = "No other adjustments are being made";
        String remoteHearingDefaultText = "";

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

        assertThat(personalisation.get("hearingRequirementVulnerabilities")).contains(
            displayFieldsPresent ? caseOfficerReviewedVulnerabilitiesDisplay
                : responseFieldsPresent ? caseOfficerReviewedVulnerabilities : vulnerabilitiesDefaultText);

        assertThat(personalisation.get("hearingRequirementMultimedia")).contains(
            displayFieldsPresent ? caseOfficerReviewedMultimediaDisplay
                : responseFieldsPresent ? caseOfficerReviewedMultimedia : multimediaDefaultText);

        assertThat(personalisation.get("hearingRequirementSingleSexCourt")).contains(
            displayFieldsPresent ? caseOfficerReviewedSingleSexCourtDisplay
                : responseFieldsPresent ? caseOfficerReviewedSingleSexCourt : singleSexCourtDefaultText);

        assertThat(personalisation.get("hearingRequirementInCameraCourt")).contains(
            displayFieldsPresent ? caseOfficerReviewedInCameraDisplay
                : responseFieldsPresent ? caseOfficerReviewedInCamera : inCameraCourtDefaultText);

        assertThat(personalisation.get("hearingRequirementOther")).contains(
            displayFieldsPresent ? caseOfficerReviewedOtherDisplay
                : responseFieldsPresent ? caseOfficerReviewedOther : otherAdjustmentsDefaultText);

        assertThat(personalisation.get("remoteVideoCallTribunalResponse")).contains(
            responseFieldsPresent ? remoteVideoCallTribunalResponse : remoteHearingDefaultText);
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

    @Test
    void should_return_appelant_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getAppellantPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    void should_return_appelant_credentials_when_all_information_given() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getApplyForCostsPersonalisation(asylumCase);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    void should_return_home_office_recipient_header_when_all_information_given() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        Map<String, String> personalisation = personalisationProvider.getHomeOfficeRecipientHeader(asylumCase);

        assertEquals("Home Office", personalisation.get(recipient));
        assertEquals(homeOfficeRefNumber, personalisation.get(recipientReferenceNumber));
    }

    @Test
    void should_return_legal_rep_recipient_header_when_all_information_given() {
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        Map<String, String> personalisation = personalisationProvider.getLegalRepRecipientHeader(asylumCase);

        assertEquals("Your", personalisation.get(recipient));
        assertEquals(legalRepReferenceNumber, personalisation.get(recipientReferenceNumber));
    }

    @Test
    void should_return_application_number_in_respond_to_costs_when_all_information_given() {
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023"), List.of(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.retrieveSelectedApplicationId(asylumCase, RESPOND_TO_COSTS_LIST);

        assertEquals("8", personalisation.get("applicationId"));
    }

    @Test
    void should_return_application_number_in_additional_evidence_applicaiton_when_all_information_given() {
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023"), List.of(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023")));

        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.retrieveSelectedApplicationId(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST);

        assertEquals("8", personalisation.get("applicationId"));
    }

    @Test
    void should_throw_an_exception_if_respond_to_costs_list_is_not_presented() {
        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisationProvider.retrieveSelectedApplicationId(asylumCase, RESPOND_TO_COSTS_LIST))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("RESPOND_TO_COSTS_LIST is not present");
    }

    @Test
    void should_throw_an_exception_if_additional_evidence_applicaiton_number_not_presented() {
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisationProvider.retrieveSelectedApplicationId(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ADD_EVIDENCE_FOR_COSTS_LIST is not present");
    }

    @Test
    void should_return_apply_for_costs_creation_date_when_all_information_given() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getApplyToCostsCreationDate(asylumCase);

        assertEquals("24 Nov 2023", personalisation.get("creationDate"));
    }

    @Test
    void should_return_type_for_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getTypeForLatestCreatedApplyForCosts(asylumCase);

        assertEquals("Wasted", personalisation.get("appliedCostsType"));
    }


    @Test
    void should_return_type_for_selected_apply_for_costs() {
        DynamicList dynamicList = new DynamicList(
            new Value("2", "Costs 8, Unreasonable costs, 15 Nov 2023"),
            List.of(
                new Value("1", "Costs 1, Wasted costs, 15 Nov 2023"),
                new Value("2", "Costs 2, Unreasonable costs, 15 Nov 2023"))
        );

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)),
            new IdValue<>("2", new ApplyForCosts("Unreasonable costs", "Home office", "Respondent", applyForCostsCreationDate))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.getTypeForSelectedApplyForCosts(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST);

        assertEquals("Unreasonable", personalisation.get("appliedCostsType"));
    }

    @Test
    void should_return_costs_decision_when_decideCostsApplicationList_is_present() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", "costsType", applyForCostsCreationDate, applyForCostsDecision))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        DynamicList respondsToCostsList = new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")));
        when(asylumCase.read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));

        Map<String, String> personalisation = personalisationProvider.getDecideCostsPersonalisation(asylumCase);

        assertEquals(applyForCostsDecision, personalisation.get("costsDecisionType"));
    }
}
