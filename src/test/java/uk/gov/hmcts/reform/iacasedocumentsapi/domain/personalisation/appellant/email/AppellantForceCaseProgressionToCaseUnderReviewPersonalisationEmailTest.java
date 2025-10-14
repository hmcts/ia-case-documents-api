package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email.AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String appellantEmailAddress = "appellantp@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private final String iaExUiFrontendUrl = "https://immigration-appeal.demo.platform.hmcts.net/start-appeal";

    private AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail
        forceCaseProgressionToCaseUnderReviewPersonalisation;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenReturn(Collections.singleton(appellantEmailAddress));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        forceCaseProgressionToCaseUnderReviewPersonalisation =
            new AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(templateId, iaExUiFrontendUrl, recipientsFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, forceCaseProgressionToCaseUnderReviewPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP_EMAIL",
            forceCaseProgressionToCaseUnderReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_recipient_email_list() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        Set<String> recipientsList = forceCaseProgressionToCaseUnderReviewPersonalisation.getRecipientsList(asylumCase);
        assertNotNull(recipientsList);
        assertThat(recipientsList).contains(appellantEmailAddress);
    }


    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                forceCaseProgressionToCaseUnderReviewPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Mockito.when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                forceCaseProgressionToCaseUnderReviewPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }

    @Test
    public void asylumCase_null_returns_error() {
        Mockito.when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewPersonalisation.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }
}
