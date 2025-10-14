package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmailTest {

    @Mock
    BailCase bailCase;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;

    private final String templateId = "someTemplateId";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String iaExUiFrontendUrl = "url";
    private final String legalRepReference = "someLegalRepReference";
    private final String legalRepName = "someLegalRepName";
    private final String legalRepFamilyName = "someLegalRepFamilyName";

    private LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(legalRepFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getEmailAddress()).thenReturn(legalRepEmailAddress);

        legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail = new LegalRepresentativeBailApplicationStartedDisposalPersonalisationEmail(
            templateId,
            iaExUiFrontendUrl,
            userDetailsProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_APPLICATION_STARTED_DISPOSAL_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        String legalRepEmailAddress = "legalRep@example.com";
        assertTrue(legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        // given
        // when
        Map<String, String> personalisation =
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation(bailCase);

        // then
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(legalRepName, personalisation.get("legalRepName"));
        assertEquals(legalRepFamilyName, personalisation.get("legalRepFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertNotNull(personalisation.get("creationDate"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        // given
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());

        // when
        Map<String, String> personalisation =
            legalRepresentativeBailApplicationStartedDisposalPersonalisationEmail.getPersonalisation(bailCase);

        // then
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("legalRepName"));
        assertEquals("", personalisation.get("legalRepFamilyName"));
    }
}
