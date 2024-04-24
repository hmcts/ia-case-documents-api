package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeDecisionWithoutHearingPersonalisationTesting {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private HomeOfficeDecisionWithoutHearingPersonalisation homeOfficeDecisionWithoutHearingPersonalisation;
    private Long caseId = 12345L;
    private String homeOfficeDecisionWithoutHearingTemplateId = "homeOfficeDecisionWithoutHearingTemplateId";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAriaListingReference = "someAriaListingReference";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedHomeOfficeEmail = "ho-taylorhouse@example.com";
    private String iaServicesPhone = "0300 123 1711";
    private String iaServicesEmail = "contactia@justice.gov.uk";
    private String iaExUiFrontendUrl = "http://localhost";
    private String subjectPrefix = "Immigration and Asylum appeal";
    private Map<String, String> customerServices = Map.of("customerServicesTelephone", iaServicesPhone,
            "customerServicesEmail", iaServicesEmail);

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReference));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));

        homeOfficeDecisionWithoutHearingPersonalisation = new HomeOfficeDecisionWithoutHearingPersonalisation(
                homeOfficeDecisionWithoutHearingTemplateId,
                emailAddressFinder,
                customerServicesProvider,
                iaExUiFrontendUrl
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(homeOfficeDecisionWithoutHearingTemplateId,
                homeOfficeDecisionWithoutHearingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_DECISION_WITHOUT_HEARING_HOME_OFFICE",
                homeOfficeDecisionWithoutHearingPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_home_office_email() {
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(mockedHomeOfficeEmail);

        assertTrue(homeOfficeDecisionWithoutHearingPersonalisation.getRecipientsList(asylumCase)
                .contains(mockedHomeOfficeEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeDecisionWithoutHearingPersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        initializePrefixes(homeOfficeDecisionWithoutHearingPersonalisation);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);


        Map<String, String> personalisation =
                homeOfficeDecisionWithoutHearingPersonalisation.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAriaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaServicesPhone, personalisation.get("customerServicesTelephone"));
        assertEquals(iaServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(subjectPrefix, personalisation.get("subjectPrefix"));
    }
}
