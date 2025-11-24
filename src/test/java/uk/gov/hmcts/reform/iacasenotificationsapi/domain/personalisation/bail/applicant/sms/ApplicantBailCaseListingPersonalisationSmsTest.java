package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicantBailCaseListingPersonalisationSmsTest {

    private final String initialTemplateId = "initialTemplateId";
    private final String relistingTemplateId = "relistingTemplateId";
    private final String conditionalBailRelistingTemplateId = "conditionalBailRelistingTemplateId";
    private String mobileNumber = "07781122334";
    private final String bailReferenceNumber = "someReferenceNumber";
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
    private ApplicantBailCaseListingPersonalisationSms applicantBailCaseListingPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.of(mobileNumber));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.of(bailHearingDateTime));
        when(hearingDetailsFinder.getBailHearingDateTime(bailCase)).thenReturn(bailHearingDateTime);
        when(hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase)).thenReturn(bailHearingLocationName);
        when(dateTimeExtractor.extractHearingDate(bailHearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(bailHearingDateTime)).thenReturn(hearingTime);

        applicantBailCaseListingPersonalisationSms =
            new ApplicantBailCaseListingPersonalisationSms(
                initialTemplateId,
                relistingTemplateId,
                conditionalBailRelistingTemplateId,
                hearingDetailsFinder,
                dateTimeExtractor
                );
    }

    @Test
    public void should_return_initial_template_id() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL));
        assertEquals(initialTemplateId, applicantBailCaseListingPersonalisationSms.getTemplateId(bailCase));
    }

    @Test
    public void should_return_relisting_template_id() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        assertEquals(relistingTemplateId, applicantBailCaseListingPersonalisationSms.getTemplateId(bailCase));
    }

    @Test
    public void should_return_conditional_bail_relisting_template_id() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, String.class)).thenReturn(Optional.of(State.DECISION_CONDITIONAL_BAIL.toString()));
        assertEquals(conditionalBailRelistingTemplateId, applicantBailCaseListingPersonalisationSms.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_CASE_LISTING_APPLICANT_SMS",
            applicantBailCaseListingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_mobile_number() {
        assertTrue(applicantBailCaseListingPersonalisationSms.getRecipientsList(bailCase).contains(mobileNumber));

        when(bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class)).thenReturn(Optional.empty());

        assertTrue(applicantBailCaseListingPersonalisationSms.getRecipientsList(bailCase).isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> applicantBailCaseListingPersonalisationSms.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            applicantBailCaseListingPersonalisationSms.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(bailHearingLocationName, personalisation.get("hearingCentre"));
    }
}
