package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailCaseListingPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeBailCaseListingPersonalisationTest {

    private Long caseId = 12345L;
    private String initialTemplateId = "initialTemplateId";
    private String initialTemplateIdWithoutLegalRep = "initialTemplateIdWithoutLegalRep";
    private String relistingTemplateId = "relistingTemplateId";
    private String relistingTemplateIdWithoutLegalRep = "relistingTemplateIdWithoutLegalRep";
    private final String conditionalBailRelistingTemplateId = "conditionalBailRelistingTemplateId";
    private final String conditionalBailRelistingTemplateIdWithoutLegalRep = "conditionalBailRelistingTemplateIdWithoutLegalRep";
    private String homeOfficeEmailAddress = "HO_user@example.com";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";
    private String bailHearingDateTime = "2024-01-01T10:29:00.000";
    private String bailHearingLocationName = "Yarl’s Wood\n" +
            "Yarl’s Wood Immigration and Asylum Hearing Centre, Twinwood Road, MK44 1FD";
    private String hearingDate = "2024-01-21";
    private String hearingTime = "10:29";
    @Mock
    BailCase bailCase;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    private HomeOfficeBailCaseListingPersonalisation homeOfficeBailCaseListingPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.of(bailHearingDateTime));
        when(hearingDetailsFinder.getBailHearingDateTime(bailCase)).thenReturn(bailHearingDateTime);
        when(hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase)).thenReturn(bailHearingLocationName);
        when(dateTimeExtractor.extractHearingDate(bailHearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(bailHearingDateTime)).thenReturn(hearingTime);
        homeOfficeBailCaseListingPersonalisation =
            new HomeOfficeBailCaseListingPersonalisation(
                initialTemplateId,
                initialTemplateIdWithoutLegalRep,
                relistingTemplateId,
                relistingTemplateIdWithoutLegalRep,
                conditionalBailRelistingTemplateId,
                conditionalBailRelistingTemplateIdWithoutLegalRep,
                homeOfficeEmailAddress,
                hearingDetailsFinder,
                dateTimeExtractor
                );
    }

    @Test
    public void should_return_initial_template_ids() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL));
        assertEquals(initialTemplateId, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(initialTemplateIdWithoutLegalRep, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));

    }

    @Test
    public void should_return_relisting_template_ids() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        assertEquals(relistingTemplateId, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(relistingTemplateIdWithoutLegalRep, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_conditional_bail_relisting_template_ids() {
        when(bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, String.class)).thenReturn(Optional.of(State.DECISION_CONDITIONAL_BAIL.toString()));
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        assertEquals(conditionalBailRelistingTemplateId, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(conditionalBailRelistingTemplateIdWithoutLegalRep, homeOfficeBailCaseListingPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_home_office_email_recipient() {
        assertTrue(homeOfficeBailCaseListingPersonalisation.getRecipientsList(bailCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_APPLICATION_CASE_LISTING_HOME_OFFICE",
            homeOfficeBailCaseListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> homeOfficeBailCaseListingPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeBailCaseListingPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(bailHearingLocationName, personalisation.get("hearingCentre"));
    }

    @Test
    public void should_return_personalisation_when_no_LR_all_information_given() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, String> personalisation =
            homeOfficeBailCaseListingPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(bailHearingLocationName, personalisation.get("hearingCentre"));
    }
}
