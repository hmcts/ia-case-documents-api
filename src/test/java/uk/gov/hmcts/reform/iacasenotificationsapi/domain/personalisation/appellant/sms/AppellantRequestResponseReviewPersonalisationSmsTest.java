package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantRequestResponseReviewPersonalisationSmsTest {


    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;


    private String requestResponseReviewWithdrawnTemplateId = "requestResponseReviewWithdrawnTemplateId";

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobileNumber = "1234445556";
    private String designatedHearingCentre = "belfast@hearingcentre.gov";

    private AppellantRequestResponseReviewPersonalisationSms
            appellantRequestResponseReviewPersonalisationSms;

    @BeforeEach
    void setUp() {

        appellantRequestResponseReviewPersonalisationSms =
                new AppellantRequestResponseReviewPersonalisationSms(
                        requestResponseReviewWithdrawnTemplateId,
                        iaAipFrontendUrl, emailAddressFinder, recipientsFinder);

    }

    @Test
    void should_return_personalisation_when_all_information_given_before_listing() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);

        Map<String, String> personalisation =
                appellantRequestResponseReviewPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantRequestResponseReviewPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
    }

    @Test
    public void should_return_given_template_id_for_decision_withdrawn() {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
                .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        assertEquals(requestResponseReviewWithdrawnTemplateId, appellantRequestResponseReviewPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_empty_template_id_for_decision_withdrawn() {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
                .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        assertEquals("", appellantRequestResponseReviewPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_RESPONSE_REVIEW_AIP_SMS",
                appellantRequestResponseReviewPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(appellantRequestResponseReviewPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantRequestResponseReviewPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }
}
