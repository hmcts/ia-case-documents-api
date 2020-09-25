package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@RunWith(MockitoJUnitRunner.class)
public class RespondentRequestResponseAmendPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock Direction direction;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String respondentReviewEmailAddress = "respondentReview@example.com";
    private String directionDueDate = "2019-08-27";
    private String expectedDirectionDueDate = "27 Aug 2019";
    private String directionExplanation = "someExplanation";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private RespondentRequestResponseAmendPersonalisation respondentRequestResponseAmendPersonalisation;

    @Before
    public void setup() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when((direction.getExplanation())).thenReturn(directionExplanation);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        respondentRequestResponseAmendPersonalisation = new RespondentRequestResponseAmendPersonalisation(
            beforeListingTemplateId,
            iaExUiFrontendUrl,
            respondentReviewEmailAddress,
            directionFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId, respondentRequestResponseAmendPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_RESPONDENT_AMEND_DIRECTION", respondentRequestResponseAmendPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(respondentRequestResponseAmendPersonalisation.getRecipientsList(asylumCase).contains(respondentReviewEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> respondentRequestResponseAmendPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_AMEND)).thenReturn(Optional.of(direction));
        Map<String, String> personalisation = respondentRequestResponseAmendPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_AMEND)).thenReturn(Optional.of(direction));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = respondentRequestResponseAmendPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {
        assertThatThrownBy(() -> respondentRequestResponseAmendPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("non-standard direction is not present");
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(respondentRequestResponseAmendPersonalisation.isAppealListed(asylumCase));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertTrue(respondentRequestResponseAmendPersonalisation.isAppealListed(asylumCase));
    }
}
