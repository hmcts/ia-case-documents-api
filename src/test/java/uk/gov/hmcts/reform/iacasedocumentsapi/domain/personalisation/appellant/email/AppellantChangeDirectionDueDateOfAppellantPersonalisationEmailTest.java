package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantChangeDirectionDueDateOfAppellantPersonalisationEmail;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeDirectionDueDateOfAppellantPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String emailTemplateId = "emailTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String ariaListingRef = "someAriaListingRef";

    private AppellantChangeDirectionDueDateOfAppellantPersonalisationEmail appellantChangeDirectionDueDateOfAppellantPersonalisationEmail;
    private String directionExplanation = "Some HO change direction due date content";
    private String dueDate = "2020-10-08";
    private String iaAipFrontendUrl = "iaAipFrontendUrl";

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantChangeDirectionDueDateOfAppellantPersonalisationEmail =
            new AppellantChangeDirectionDueDateOfAppellantPersonalisationEmail(
                emailTemplateId,
                iaAipFrontendUrl,
                personalisationProvider,
                recipientsFinder,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_APPELLANT_EMAIL",
            appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_before_listing() {

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback))
            .thenReturn(getPersonalisationForAppellant(mockedAppealReferenceNumber, mockedAppealHomeOfficeReferenceNumber, mockedAppellantGivenNames, mockedAppellantFamilyName));

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_after_listing() {

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingRef));
        when(personalisationProvider.getPersonalisation(callback))
            .thenReturn(getPersonalisationForAppellant(mockedAppealReferenceNumber, mockedAppealHomeOfficeReferenceNumber, mockedAppellantGivenNames, mockedAppellantFamilyName));

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals("\nListing reference: " + ariaListingRef, personalisation.get("listingReferenceLine"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant("", "", "", ""));

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfAppellantPersonalisationEmail.getPersonalisation(callback);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals("", personalisation.get("listingReferenceLine"));

    }

    private Map<String, String> getPersonalisationForAppellant(String mockedAppealReferenceNumber, String mockedAppealHomeOfficeReferenceNumber,
                                                               String mockedAppellantGivenNames, String mockedAppellantFamilyName) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", mockedAppealReferenceNumber)
            .put("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .put("appellantGivenNames", mockedAppellantGivenNames)
            .put("appellantFamilyName", mockedAppellantFamilyName)
            .put("ariaListingReference", ariaListingRef)
            .put("explanation", directionExplanation)
            .put("dueDate", LocalDate
                .parse(dueDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            )
            .build();
    }

}

