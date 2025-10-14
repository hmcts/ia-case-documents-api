package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATED_DECISION_MAKER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_REASON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantReinstateAppealPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AppealService appealService;

    private Long caseId = 12345L;
    private String beforeListingEmailTemplateId = "beforeListingEmailTemplateId";
    private String afterListingEmailTemplateId = "afterListingEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String reinstateAppealDate = "2020-10-08";
    private String reinstateAppealReason = "someReason";
    private String reinstatedDecisionMaker = "someDecisionMaker";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String ariaListingRef = "someAriaListingRef";
    private String removeAppealFromOnlineReason = "someRemoveAppeal reason";

    private AppellantReinstateAppealPersonalisationEmail appellantAppealExitedOnlinePersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.of(reinstateAppealReason));
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of(reinstatedDecisionMaker));

        appellantAppealExitedOnlinePersonalisationEmail =
                new AppellantReinstateAppealPersonalisationEmail(
                        beforeListingEmailTemplateId,
                        afterListingEmailTemplateId,
                        iaAipFrontendUrl,
                        customerServicesProvider,
                        appealService,
                        recipientsFinder
                );
    }

    @Test
    public void should_return_given_template_id_for_before_listing() {
        assertEquals(beforeListingEmailTemplateId, appellantAppealExitedOnlinePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_after_listing() {

        when(appealService.isAppealListed(asylumCase))
                .thenReturn(true);

        assertEquals(afterListingEmailTemplateId, appellantAppealExitedOnlinePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REINSTATE_APPEAL_AIP_APPELLANT_EMAIL",
                appellantAppealExitedOnlinePersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantAppealExitedOnlinePersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantAppealExitedOnlinePersonalisationEmail.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_before_listing(YesOrNo isAda) {

        initializePrefixes(appellantAppealExitedOnlinePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantAppealExitedOnlinePersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("reinstateAppealDate"));
        assertEquals(reinstateAppealReason, personalisation.get("reinstateAppealReason"));
        assertEquals(reinstatedDecisionMaker, personalisation.get("reinstatedDecisionMaker"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_after_listing(YesOrNo isAda) {

        initializePrefixes(appellantAppealExitedOnlinePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BELFAST));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingRef));

        Map<String, String> personalisation =
                appellantAppealExitedOnlinePersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("reinstateAppealDate"));
        assertEquals(reinstateAppealReason, personalisation.get("reinstateAppealReason"));
        assertEquals(reinstatedDecisionMaker, personalisation.get("reinstatedDecisionMaker"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(ariaListingRef, personalisation.get("ariaListingReference"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantAppealExitedOnlinePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantAppealExitedOnlinePersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("reinstateAppealDate"));
        assertEquals("No reason given", personalisation.get("reinstateAppealReason"));
        assertEquals("", personalisation.get("reinstatedDecisionMaker"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
