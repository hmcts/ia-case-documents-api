package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeFtpaSubmittedPersonalisationTest {

    @Mock PersonalisationProvider personalisationProvider;
    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;
    @Mock CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "ftpaSumbittedTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalrep@example.com";
    private String ariaListingReference = "someAriaListingReference";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeFtpaSubmittedPersonalisation legalRepresentativeFtpaSubmittedPersonalisation;

    @Before
    public void setUp() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeFtpaSubmittedPersonalisation = new LegalRepresentativeFtpaSubmittedPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_email_address() {

        assertThat(legalRepresentativeFtpaSubmittedPersonalisation.getRecipientsList(asylumCase)).isEqualTo(Collections.singleton(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_email_address_is_null() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> legalRepresentativeFtpaSubmittedPersonalisation.getRecipientsList(asylumCase))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_given_template_id() {

        assertThat(legalRepresentativeFtpaSubmittedPersonalisation.getTemplateId()).isEqualTo(templateId);
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(legalRepresentativeFtpaSubmittedPersonalisation.getReferenceId(caseId)).isEqualTo(caseId + "_FTPA_SUBMITTED_LEGAL_REP");
    }

    @Test
    public void should_return_given_personalisation() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());
        Map<String, String> expectedPersonalisation = legalRepresentativeFtpaSubmittedPersonalisation.getPersonalisation(callback);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(() -> legalRepresentativeFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReferenceNumber", "CASE001")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
