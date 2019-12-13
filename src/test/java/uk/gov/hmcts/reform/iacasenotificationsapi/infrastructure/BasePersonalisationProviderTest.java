package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@RunWith(MockitoJUnitRunner.class)
public class BasePersonalisationProviderTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock AsylumCase asylumCase;
    @Mock AsylumCase asylumCaseBefore;
    @Mock HearingDetailsFinder hearingDetailsFinder;
    @Mock DateTimeExtractor dateTimeExtractor;
    @Mock Direction direction;
    @Mock DirectionFinder directionFinder;

    private String iaCcdFrontendUrl = "http://localhost";

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

    private String oldHearingCentreName = HearingCentre.MANCHESTER.toString();
    private String oldHearingDateTime = "2019-08-20T14:25:15.000";
    private String oldHearingDate = "2019-08-20";

    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsOther = "someRequirementsOther";

    private String directionExplanation = "someExplanation";
    private String directionDueDate = "2019-10-29";

    private BasePersonalisationProvider basePersonalisationProvider;

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

        Mockito.when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));
        Mockito.when(direction.getDateDue()).thenReturn(directionDueDate);
        Mockito.when(direction.getExplanation()).thenReturn(directionExplanation);

        basePersonalisationProvider = new BasePersonalisationProvider(
            iaCcdFrontendUrl,
            hearingDetailsFinder,
            directionFinder,
            dateTimeExtractor
        );
    }

    @Test
    public void should_return_edit_case_listing_personalisation() {

        Map<String, String> personalisation = basePersonalisationProvider.getEditCaseListingPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_non_direction_personalisation() {

        Map<String, String> personalisation = basePersonalisationProvider.getNonStandardDirectionPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_submitted_hearing_requirements_personalisation() {

        Map<String, String> personalisation = basePersonalisationProvider.getSubmittedHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}
