package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantDecideAnApplicationPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantDecideAnApplicationPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;

    private Long caseId = 12345L;
    private String refusedSmsTemplateId = "someRefusedSmsTemplateId";
    private String grantedSmslTemplateId = "someGrantedSmsTemplateId";
    private String otherPartySmsTemplateId = "otherPartySmsTempateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String applicationType = "someApplicationType";
    private String applicationTypePhrase = "some application type";
    private String decisionMaker = "someDecisionMaker";
    private final String citizenUser = "citizen";
    private final String homeOfficeUser = "caseworker-ia-homeofficelart";

    private AppellantDecideAnApplicationPersonalisationSms appellantDecideAnApplicationPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantDecideAnApplicationPersonalisationSms = new AppellantDecideAnApplicationPersonalisationSms(
            refusedSmsTemplateId,
            grantedSmslTemplateId,
            otherPartySmsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
            makeAnApplicationService,
            userDetailsProvider);
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.ofNullable(makeAnApplication));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplication.getDecisionMaker()).thenReturn(decisionMaker);
        when(makeAnApplicationService.mapApplicationTypeToPhrase(makeAnApplication))
            .thenReturn(applicationTypePhrase);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
    }


    @Test
    public void should_return_refused_template_id() {
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");

        assertEquals(refusedSmsTemplateId,
                appellantDecideAnApplicationPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_granted_template_id() {
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getApplicantRole()).thenReturn(citizenUser);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");

        assertEquals(grantedSmslTemplateId,
                appellantDecideAnApplicationPersonalisationSms.getTemplateId(asylumCase));
    }

    public void should_return_other_party_template_id() {
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeUser);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");

        assertEquals(otherPartySmsTemplateId,
                appellantDecideAnApplicationPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_APPELLANT_AIP_SMS",
            appellantDecideAnApplicationPersonalisationSms.getReferenceId(caseId));
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

        assertTrue(appellantDecideAnApplicationPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantDecideAnApplicationPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    public void should_return_personalisation_when_all_information_given_and_decision_refused(String user) {
        when(userDetails.getRoles()).thenReturn(List.of(user));
        String decision = "Refused";
        when(makeAnApplication.getDecision()).thenReturn(decision);

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));
        assertEquals(decision, personalisation.get("decision"));
        assertEquals(decisionMaker, personalisation.get("decision maker role"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);

    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    public void should_return_personalisation_when_all_information_given_and_decision_granted(String user) {
        when(userDetails.getRoles()).thenReturn(List.of(user));
        when(makeAnApplication.getDecision()).thenReturn("Granted");

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(userDetails.getRoles()).thenReturn(List.of(citizenUser));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");
        when(makeAnApplication.getDecisionMaker()).thenReturn("");
        when(makeAnApplication.getDecision()).thenReturn("Refused");

        Map<String, String> personalisation =
            appellantDecideAnApplicationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals("", personalisation.get("applicationType"));
        assertEquals("", personalisation.get("decision maker role"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, true);
    }
}
