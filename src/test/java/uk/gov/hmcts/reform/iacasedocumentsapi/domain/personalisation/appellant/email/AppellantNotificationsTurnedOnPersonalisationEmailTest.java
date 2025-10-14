package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantNotificationsTurnedOnPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNotificationsTurnedOnPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PinInPostDetails pinInPostDetails;
    private final String representedTemplateId = "representedTemplateId";
    private final String unrepresentedTemplateId = "unrepresentedTemplateId";
    private final Long caseId = 12345L;
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String legalRepReferenceEjp = "someLegalRepReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private String dateOfBirth = "2020-03-01";
    private final String expectedDateOfBirth = "1 Mar 2020";
    private final String appellantEmailEjp = "appellant@example.com";
    private final String ccdReferenceNumberForDisplay = "someRefNumber";
    private final String homeOfficeRef = "homeOfficeRef";
    private final String customerServicesTelephone = "customerServicesTelephone";
    private final String customerServicesEmail = "customerServicesEmail";
    private final String securityCode = "securityCode";
    private final String validDate = "2024-03-01";
    private final String expectedValidDate = "1 Mar 2024";

    private AppellantNotificationsTurnedOnPersonalisationEmail appellantNotificationsTurnedOnPersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepReferenceEjp));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(appellantEmailEjp));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumberForDisplay));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);

        appellantNotificationsTurnedOnPersonalisationEmail = new AppellantNotificationsTurnedOnPersonalisationEmail(
            representedTemplateId,
            unrepresentedTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_represented_template_id() {
        assertEquals(representedTemplateId,
            appellantNotificationsTurnedOnPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_unrepresented_template_id() {
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());

        assertEquals(unrepresentedTemplateId,
            appellantNotificationsTurnedOnPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(appellantNotificationsTurnedOnPersonalisationEmail.getReferenceId(caseId))
            .isEqualTo(caseId + "_APPELLANT_NOTIFICATIONS_TURNED_ON");
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(
            appellantNotificationsTurnedOnPersonalisationEmail.getRecipientsList(asylumCase).contains(
                appellantEmailEjp));
    }

    @Test
    public void should_return_empty_set_when_email_address_is_null() {
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.empty());

        assertEquals(Collections.emptySet(),
            appellantNotificationsTurnedOnPersonalisationEmail.getRecipientsList(asylumCase));
    }



    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantNotificationsTurnedOnPersonalisationEmail.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            appellantNotificationsTurnedOnPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("\nListing reference: " + ariaListingReference, personalisation.get("listingReferenceLine"));
        assertEquals(homeOfficeRef, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ccdReferenceNumberForDisplay, personalisation.get("ccdReferenceNumberForDisplay"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(expectedDateOfBirth, personalisation.get("dateOfBirth"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(securityCode, personalisation.get("securityCode"));
        assertEquals(expectedValidDate, personalisation.get("validDate"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
