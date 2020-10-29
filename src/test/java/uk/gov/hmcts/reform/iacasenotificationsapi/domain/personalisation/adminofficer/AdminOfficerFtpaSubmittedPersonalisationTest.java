package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AdminOfficerFtpaSubmittedPersonalisationTest {

    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "ftpaSubmittedTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String adminOfficerEmailAddress = "adminOfficer@example.com";
    private String ariaListingReference = "someAriaListingReference";

    private AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation;

    @BeforeEach
    public void setUp() {

        adminOfficerFtpaSubmittedPersonalisation = new AdminOfficerFtpaSubmittedPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            adminOfficerEmailAddress);
    }

    @Test
    public void should_return_given_personalisation() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());
        Map<String, String> expectedPersonalisation =
            adminOfficerFtpaSubmittedPersonalisation.getPersonalisation(callback);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> adminOfficerFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(adminOfficerFtpaSubmittedPersonalisation.getReferenceId(caseId))
            .isEqualTo(caseId + "_FTPA_SUBMITTED_ADMIN_OFFICER");
    }

    @Test
    public void should_return_given_email_address() {

        assertThat(adminOfficerFtpaSubmittedPersonalisation.getRecipientsList(asylumCase))
            .isEqualTo(Collections.singleton(adminOfficerEmailAddress));
    }

    @Test
    public void should_return_given_template_id() {

        assertThat(adminOfficerFtpaSubmittedPersonalisation.getTemplateId()).isEqualTo(templateId);
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .build();
    }
}
