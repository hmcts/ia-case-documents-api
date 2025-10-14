package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeEditListingPersonalisationTest {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private Long caseId = 12345L;
    private String adaTemplateId = "adaTemplateId";
    private String nonAdaTemplateId = "nonAdaTemplateId";
    private String templateIdRemoteHearing = "remoteTemplateId";
    private String listAssistHearingTemplateId = "listAssistHearingTemplateId";
    private String listAssistHearingTemplateIdRemoteHearing = "listAssistHearingRemoteTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String hearingCentreAddress = "some hearing centre address";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";

    private String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private String hearingCentreName = HearingCentre.TAYLOR_HOUSE.toString();
    private String remoteVideoCallTribunalResponse = "some tribunal response";
    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsOther = "someRequirementsOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeEditListingPersonalisation = new LegalRepresentativeEditListingPersonalisation(
            nonAdaTemplateId,
            adaTemplateId,
            templateIdRemoteHearing,
            listAssistHearingTemplateId,
            listAssistHearingTemplateIdRemoteHearing,
            personalisationProvider,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertEquals(nonAdaTemplateId, legalRepresentativeEditListingPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertEquals(templateIdRemoteHearing, legalRepresentativeEditListingPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(adaTemplateId, legalRepresentativeEditListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_RE_LISTED_LEGAL_REPRESENTATIVE",
            legalRepresentativeEditListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(
            legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeEditListingPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeEditListingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        initializePrefixes(legalRepresentativeEditListingPersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_optional_fields_are_blank(YesOrNo isAda) {
        initializePrefixes(legalRepresentativeEditListingPersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());

        Map<String, String> personalisation =
            legalRepresentativeEditListingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", hearingCentreName)
            .put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse)
            .put("hearingRequirementVulnerabilities", requirementsVulnerabilities)
            .put("hearingRequirementMultimedia", requirementsMultimedia)
            .put("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
            .put("hearingRequirementInCameraCourt", requirementsInCamera)
            .put("hearingRequirementOther", requirementsOther)
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("ariaListingReference", "")
            .put("homeOfficeReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingCentreName", "")
            .put("remoteVideoCallTribunalResponse", "")
            .put("hearingRequirementVulnerabilities", "")
            .put("hearingRequirementMultimedia", "")
            .put("hearingRequirementSingleSexCourt", "")
            .put("hearingRequirementInCameraCourt", "")
            .put("hearingRequirementOther", "")
            .put("oldHearingCentre", "")
            .put(HEARING_CENTRE_ADDRESS, "")
            .put("customerServicesTelephone", "")
            .put("customerServicesEmail", "")
            .build();
    }
}
