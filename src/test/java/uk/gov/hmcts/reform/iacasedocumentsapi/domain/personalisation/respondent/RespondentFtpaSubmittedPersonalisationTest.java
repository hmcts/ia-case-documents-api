package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class RespondentFtpaSubmittedPersonalisationTest {

    @Mock PersonalisationProvider personalisationProvider;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;
    @Mock CustomerServicesProvider customerServicesProvider;
    @Mock EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String templateId = "templateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String respondentEmailAddress = "respondent@example.com";
    private String ariaListingReference = "someAriaListingReference";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation;

    @BeforeEach
    public void setup() {

        respondentFtpaSubmittedPersonalisation = new RespondentFtpaSubmittedPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            customerServicesProvider,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_give_reference_id() {

        assertEquals(caseId + "_FTPA_SUBMITTED_RESPONDENT",
            respondentFtpaSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, respondentFtpaSubmittedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_recipient_email_id() {
        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase)).thenReturn(respondentEmailAddress);
        assertEquals(Collections.singleton(respondentEmailAddress), respondentFtpaSubmittedPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_given_personalisation(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentFtpaSubmittedPersonalisation);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());

        Map<String, String> expectedPersonalisation =
            respondentFtpaSubmittedPersonalisation.getPersonalisation(callback);

        assertThat(expectedPersonalisation).isEqualToComparingOnlyGivenFields(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_personalisation_when_callback_is_null() {

        assertThatThrownBy(() -> respondentFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReference", "A1234567")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
