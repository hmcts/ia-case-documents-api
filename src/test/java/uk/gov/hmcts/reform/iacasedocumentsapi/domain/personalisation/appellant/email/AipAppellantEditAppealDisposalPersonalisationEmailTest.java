package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantEditAppealDisposalPersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;

    private final Long caseId = 12345L;
    private final String emailTemplateId = "someEmailTemplateId";
    private final String mockedAppellantEmailAddress = "appelant@example.net";

    private AipAppellantEditAppealDisposalPersonalisationEmail aipAppellantEditAppealDisposalPersonalisationEmail;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("someHomeOfficeReferenceNumber"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("someAppellantGivenNames"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("someAppellantFamilyName"));

        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(mockedAppellantEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn("555 555 555");
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn("cust.services@example.com");

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(mockedAppellantEmailAddress);

        String iaAipFrontendUrl = "http://localhost";
        aipAppellantEditAppealDisposalPersonalisationEmail = new AipAppellantEditAppealDisposalPersonalisationEmail(
            emailTemplateId,
            iaAipFrontendUrl,
            customerServicesProvider,
            userDetailsProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, aipAppellantEditAppealDisposalPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_EDITED_APPELLANT_AIP_DISPOSAL",
            aipAppellantEditAppealDisposalPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_in_asylum_case_in_non_aip_case() {
        // given
        when(asylumCase.read(EMAIL))
            .thenReturn(Optional.of(mockedAppellantEmailAddress));

        // when
        // then
        assertTrue(aipAppellantEditAppealDisposalPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        // given
        // when
        Map<String, String> personalisation =
                aipAppellantEditAppealDisposalPersonalisationEmail.getPersonalisation(callback);

        // then
        assertEquals("someAppellantGivenNames someAppellantFamilyName", personalisation.get("appellantFullName"));
        assertEquals("someHomeOfficeReferenceNumber", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("http://localhost", personalisation.get("linkToOnlineService"));
        assertNotNull(personalisation.get("editingDate"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        // given
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        // when
        Map<String, String> personalisation =
                aipAppellantEditAppealDisposalPersonalisationEmail.getPersonalisation(callback);

        // then
        assertThat(personalisation).isNotEmpty();
        assertEquals("Appellant", personalisation.get("appellantFullName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
    }
}
