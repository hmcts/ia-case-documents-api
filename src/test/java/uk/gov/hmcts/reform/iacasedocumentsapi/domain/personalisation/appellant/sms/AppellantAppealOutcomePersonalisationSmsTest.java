package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantAppealOutcomePersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantAppealOutcomePersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String appealOutcomeDismissedAppellantTemplateId = "appealOutcomeDismissedAppellantTemplateId";
    private String appealOutcomeAllowedAppellantTemplateId = "appealOutcomeAllowedAppellantTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String twoWeeksToAppeal = "14 days";
    private String fourWeeksToAppeal = "28 days";

    private AppellantAppealOutcomePersonalisationSms appellantAppealOutcomePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantAppealOutcomePersonalisationSms = new AppellantAppealOutcomePersonalisationSms(
                appealOutcomeAllowedAppellantTemplateId,
            appealOutcomeDismissedAppellantTemplateId,
                iaAipFrontendUrl,
                recipientsFinder);
    }


    @Test
    public void should_return_given_template_id_for_appeal_allowed() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.of(AppealDecision.ALLOWED));
        assertEquals(appealOutcomeAllowedAppellantTemplateId,
            appellantAppealOutcomePersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_appeal_dismissed() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.of(AppealDecision.DISMISSED));
        assertEquals(appealOutcomeDismissedAppellantTemplateId,
            appellantAppealOutcomePersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_OUTCOME_AIP_APPELLANT_SMS",
            appellantAppealOutcomePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YesOrNo.YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantAppealOutcomePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantAppealOutcomePersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_in_uk() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            appellantAppealOutcomePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(twoWeeksToAppeal, personalisation.get("14 or 28 days after judge uploads decisions and reasons document"));
        assertEquals(iaAipFrontendUrl, personalisation.get("link to timeline"));

    }

    @Test
    public void should_return_personalisation_when_in_ooc() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation =
            appellantAppealOutcomePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(fourWeeksToAppeal, personalisation.get("14 or 28 days after judge uploads decisions and reasons document"));
        assertEquals(iaAipFrontendUrl, personalisation.get("link to timeline"));

    }
}
