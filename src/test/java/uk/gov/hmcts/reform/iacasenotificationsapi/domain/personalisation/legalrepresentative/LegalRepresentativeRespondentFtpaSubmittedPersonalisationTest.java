package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeRespondentFtpaSubmittedPersonalisationTest {

    @Mock PersonalisationProvider personalisationProvider;
    @Mock Callback<AsylumCase> callback;
    @Mock AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "ftpaSumbittedTemplateId";
    private String legalRepEmailAddress = "legalrep@example.com";

    private LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation;

    @Before
    public void setUp() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeRespondentFtpaSubmittedPersonalisation = new LegalRepresentativeRespondentFtpaSubmittedPersonalisation(templateId, personalisationProvider);
    }

    @Test
    public void should_return_given_email_address() {

        assertThat(legalRepresentativeRespondentFtpaSubmittedPersonalisation.getRecipientsList(asylumCase)).isEqualTo(Collections.singleton(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_email_address_is_null() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> legalRepresentativeRespondentFtpaSubmittedPersonalisation.getRecipientsList(asylumCase))
            .hasMessage("legalRepresentativeEmailAddress is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_given_template_id() {

        assertThat(legalRepresentativeRespondentFtpaSubmittedPersonalisation.getTemplateId()).isEqualTo(templateId);
    }

    @Test
    public void should_return_given_reference_id() {

        assertThat(legalRepresentativeRespondentFtpaSubmittedPersonalisation.getReferenceId(caseId)).isEqualTo(caseId + "_LEGAL_REP_RESPONDENT_FTPA_SUBMITTED");
    }

    @Test
    public void should_return_given_personalisation() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());
        Map<String, String> expectedPersonalisation = legalRepresentativeRespondentFtpaSubmittedPersonalisation.getPersonalisation(callback);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(() -> legalRepresentativeRespondentFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("legalRepReferenceNumber", "CASE001")
            .put("homeOfficeReference", "A1234567")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .build();
    }
}
