package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@RunWith(MockitoJUnitRunner.class)
public class CaseOfficerMakeAnApplicationPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock AppealService appealService;
    @Mock MakeAnApplicationService makeAnApplicationService;

    private Long caseId = 12345L;
    private String makeAnApplicationCaseOfficerBeforeListingTemplateId = "beforeListTemplateId";
    private String makeAnApplicationCaseOfficerAfterListingTemplateId = "afterListTemplateId";
    private String makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId = "judgeReviewBeforeListTemplateId";
    private String makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId = "judgeReviewAfterListTemplateId";

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerMakeAnApplicationPersonalisation caseOfficerMakeAnApplicationPersonalisation;

    @Before
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        caseOfficerMakeAnApplicationPersonalisation = new CaseOfficerMakeAnApplicationPersonalisation(
            makeAnApplicationCaseOfficerBeforeListingTemplateId,
            makeAnApplicationCaseOfficerAfterListingTemplateId,
            makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId,
            makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId,
            iaExUiFrontendUrl,
            emailAddressFinder,
            appealService,
            makeAnApplicationService
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        when(makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase)).thenReturn("");
        assertEquals(makeAnApplicationCaseOfficerBeforeListingTemplateId, caseOfficerMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationCaseOfficerAfterListingTemplateId, caseOfficerMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase)).thenReturn("Judge's review of application decision");
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(makeAnApplicationCaseOfficerJudgeReviewBeforeListingTemplateId, caseOfficerMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationCaseOfficerJudgeReviewAfterListingTemplateId, caseOfficerMakeAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_MAKE_AN_APPLICATION_CASE_OFFICER", caseOfficerMakeAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(caseOfficerMakeAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(hearingCentreEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerMakeAnApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = caseOfficerMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = caseOfficerMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
