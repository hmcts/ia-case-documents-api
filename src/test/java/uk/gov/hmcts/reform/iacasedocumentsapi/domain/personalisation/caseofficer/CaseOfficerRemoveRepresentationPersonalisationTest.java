package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerRemoveRepresentationPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AppealService appealService;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PinInPostDetails pinInPostDetails;

    private Long ccdCaseId = 12345L;
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreEmailAddress = "hearingCentre@example.com";
    private final String hearingDateTime = "2019-08-27T14:25:15.000";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private String securityCode = "securityCode";
    private String validDate = "2022-12-31";
    private String validDateFormatted = "31 Dec 2022";
    private String iaAipFrontendUrl = "iaAipFrontendUrl/";
    private String iaAipPathToSelfRepresentation = "iaAipPathToSelfRepresentation";
    private String linkToPiPStartPage = "iaAipFrontendUrl/iaAipPathToSelfRepresentation";

    private CaseOfficerRemoveRepresentationPersonalisation caseOfficerRemoveRepresentationPersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));

        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);

        caseOfficerRemoveRepresentationPersonalisation = new CaseOfficerRemoveRepresentationPersonalisation(
            iaAipFrontendUrl,
            iaAipPathToSelfRepresentation,
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            appealService,
            emailAddressFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId, caseOfficerRemoveRepresentationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, caseOfficerRemoveRepresentationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_REMOVE_REPRESENTATION_CASE_OFFICER", caseOfficerRemoveRepresentationPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_given_email_address_from_lookup_map() {
        assertTrue(
                caseOfficerRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(hearingCentreEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerRemoveRepresentationPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_and_case_listed(YesOrNo isAda) {
        initializePrefixes(caseOfficerRemoveRepresentationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);

        Map<String, String> personalisation = caseOfficerRemoveRepresentationPersonalisation.getPersonalisation(callback);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(String.valueOf(ccdCaseId), personalisation.get("ccdCaseId"));
        assertEquals(linkToPiPStartPage, personalisation.get("linkToPiPStartPage"));
        assertEquals(securityCode, personalisation.get("securityCode"));
        assertEquals(validDateFormatted, personalisation.get("validDate"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_and_case_not_listed(YesOrNo isAda) {

        initializePrefixes(caseOfficerRemoveRepresentationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        Map<String, String> personalisation = caseOfficerRemoveRepresentationPersonalisation.getPersonalisation(callback);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(String.valueOf(ccdCaseId), personalisation.get("ccdCaseId"));
        assertEquals(linkToPiPStartPage, personalisation.get("linkToPiPStartPage"));
        assertEquals(securityCode, personalisation.get("securityCode"));
        assertEquals(validDateFormatted, personalisation.get("validDate"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerRemoveRepresentationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = caseOfficerRemoveRepresentationPersonalisation.getPersonalisation(callback);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("securityCode"));
        assertEquals("", personalisation.get("validDate"));
        assertEquals(String.valueOf(ccdCaseId), personalisation.get("ccdCaseId"));
        assertEquals(linkToPiPStartPage, personalisation.get("linkToPiPStartPage"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
