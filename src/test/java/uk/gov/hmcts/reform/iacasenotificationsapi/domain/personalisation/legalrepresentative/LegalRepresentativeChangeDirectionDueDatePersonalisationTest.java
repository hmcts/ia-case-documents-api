package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeChangeDirectionDueDatePersonalisationTest {

    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock EmailAddressFinder emailAddressFinder;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String afterListingTemplateId = "afterListingTemplateId";
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "hmctsReference";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepReference = "legalRepresentativeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation;

    @Before
    public void setUp() {
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);

        legalRepresentativeChangeDirectionDueDatePersonalisation = new LegalRepresentativeChangeDirectionDueDatePersonalisation(
            afterListingTemplateId,
            beforeListingTemplateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            emailAddressFinder,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_the_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(legalRepEmailAddress), legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_given_before_listing_template_id() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, State.class))
            .thenReturn(Optional.of(State.CASE_BUILDING));

        assertEquals(beforeListingTemplateId, legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_after_listing_template_id() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, State.class))
            .thenReturn(Optional.of(State.FINAL_BUNDLING));

        assertEquals(afterListingTemplateId, legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_throw_exception_if_current_visible_state_to_legal_rep_is_not_present() {

        assertThatThrownBy(() -> legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("currentCaseStateVisibleToLegalRepresentative flag is not present");
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE", legalRepresentativeChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForLegalRep());

        Map<String, String> personalisation = legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(callback);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalistaion_when_case_is_null() {
        assertThatThrownBy(() -> legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForLegalRep() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReference", legalRepReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
