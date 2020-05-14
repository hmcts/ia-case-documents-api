package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String legalRepEmailAddress = "legalrep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepReferenceNumber = "someLegalRepReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyNames = "someAppellantFamilyNames";
    private String iaExUiFrontendUrl = "http://localhost";
    private String directionExplanation = "someExplanation";
    private String directionDueDate = "2019-10-29";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation;

    @Before
    public void setUp() {

        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation = new LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertEquals(afterListingTemplateId, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_NON_STANDARD_DIRECTION_OF_HOME_OFFICE", legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyNames)
            .put("iaExUiFrontendUrl", iaExUiFrontendUrl)
            .put("explanation", directionExplanation)
            .put("dueDate", directionDueDate)
            .build();
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.isAppealListed(asylumCase));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertTrue(legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation.isAppealListed(asylumCase));
    }
}
