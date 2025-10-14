package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SOURCE_OF_REMITTAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmail
        appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail;
    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "https://aip-url";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String templateId = "templateId";
    private String iaServicesPhone = "0100000000";
    private String iaServicesEmail = "services@email.com";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantEmail = "test@test.com";
    private final String validDate = "2024-03-01";
    private final String validDateShown = "1 Mar 2024";
    private final String securityCode = "securityCode";
    private String ccdReferenceNumber = "0000 0000 0000 0001";

    private SourceOfRemittal sourceOfRemittal = SourceOfRemittal.UPPER_TRIBUNAL;
    private Map<String, String> customerServices = Map.of("customerServicesTelephone", iaServicesPhone,
        "customerServicesEmail", iaServicesEmail);

    @Mock
    PinInPostDetails pinInPostDetails;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(sourceOfRemittal));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)).thenReturn(Optional.ofNullable(appellantEmail));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));

        appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail = new AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmail(
            templateId,
            iaAipFrontendUrl,
            customerServicesProvider);
    }

    @Test
    public void should_return_given_email() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));

        assertEquals(Collections.singleton(appellantEmail),
            appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getRecipientsList(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_NON_DETAINED_APPELLANT_EMAIL",
            appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(iaServicesPhone, personalisation.get("customerServicesTelephone"));
        assertEquals(iaServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("urlLink"));
        assertEquals(ccdReferenceNumber, personalisation.get("ccdRefNumber"));
        assertEquals(securityCode, personalisation.get("securityCode"));
        assertEquals(validDateShown, personalisation.get("expirationDate"));

    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_error_if_remittal_source_missing() {
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appellantMarkAppealAsRemittedNonDetainedPersonalisationEmail.getPersonalisation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("sourceOfRemittal is not present");
    }

}
