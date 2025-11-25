package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AppellantNonStandardDirectionOfHomeOfficePersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;


    private Long caseId = 12345L;
    private String emailBeforeTemplateId = "someEmailTemplateId";
    private String emailAfterTemplateId = "someEmailTemplateId";
    private String toAppellantAndRespondentAfterTemplateId = "someEmailTemplateId";
    private String toAppellantAndRespondentBeforeTemplateId = "someEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail appellantNonStandardDirectionPersonalisationEmail;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantNonStandardDirectionPersonalisationEmail = new AppellantNonStandardDirectionOfHomeOfficePersonalisationEmail(
                emailBeforeTemplateId,
                emailAfterTemplateId,
                toAppellantAndRespondentAfterTemplateId,
                toAppellantAndRespondentBeforeTemplateId,
                iaAipFrontendUrl,
                personalisationProvider,
                customerServicesProvider,
                recipientsFinder,
                directionFinder);
    }

    @Test
    public void should_return_given_template_id_before_listing() {
        assertEquals(emailAfterTemplateId,
                appellantNonStandardDirectionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_after_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BELFAST));
        assertEquals(emailAfterTemplateId,
                appellantNonStandardDirectionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_before_listing_to_appellant_and_respondent() {
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));
        when(direction.getParties()).thenReturn(Parties.APPELLANT_AND_RESPONDENT);

        assertEquals(emailAfterTemplateId,
                appellantNonStandardDirectionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_after_listing_to_appellant_and_respondent() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BELFAST));
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));
        when(direction.getParties()).thenReturn(Parties.APPELLANT_AND_RESPONDENT);

        assertEquals(emailAfterTemplateId,
                appellantNonStandardDirectionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_NON_STANDARD_DIRECTION_OF_HOME_OFFICE_EMAIL",
                appellantNonStandardDirectionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantNonStandardDirectionPersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantNonStandardDirectionPersonalisationEmail.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
                .thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = appellantNonStandardDirectionPersonalisationEmail.getPersonalisation(callback);

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToOnlineService"));

    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", mockedAppealReferenceNumber)
                .put("ariaListingReference", mockedAppealHomeOfficeReferenceNumber)
                .put("appellantGivenNames", mockedAppellantGivenNames)
                .put("appellantFamilyName", mockedAppellantFamilyName)
                .build();
    }
}
