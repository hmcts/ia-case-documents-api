package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantFtpaApplicationDecisionPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "frontendHyperlink";
    private String referenceNumber = "someReferenceNumber";
    private String respondentGrantedPartiallyGrantedEmailTemplateId = "respondentGrantedPartiallyGrantedEmailTemplateId";
    private String respondentNotAdmittedEmailTemplateId = "respondentNotAdmittedEmailTemplateId";
    private String respondentRefusedEmailTemplateId = "respondentRefusedEmailTemplateId";
    private String appellantGrantedEmailTemplateId = "appellantGrantedEmailTemplateId";
    private String appellantPartiallyGrantedEmailTemplateId = "appellantPartiallyGrantedEmailTemplateId";
    private String appellantNotAdmittedEmailTemplateId = "appellantNotAdmittedEmailTemplateId";
    private String appellantRefusedEmailTemplateId = "appellantRefusedEmailTemplateId";
    private String mockedAppellantMobilePhone = "07123456789";
    private long oocDays = 28;
    private long inCountryDays = 14;
    private LocalDate today = LocalDate.now();
    private String expectedDueDateOoc = today.plusDays(oocDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    private String expectedDueDateInCountry = today.plusDays(inCountryDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

    private AppellantFtpaApplicationDecisionPersonalisationSms appellantFtpaApplicationDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {
        appellantFtpaApplicationDecisionPersonalisationSms = new AppellantFtpaApplicationDecisionPersonalisationSms(
            respondentGrantedPartiallyGrantedEmailTemplateId,
            respondentNotAdmittedEmailTemplateId,
            respondentRefusedEmailTemplateId,
            appellantGrantedEmailTemplateId,
            appellantPartiallyGrantedEmailTemplateId,
            appellantNotAdmittedEmailTemplateId,
            appellantRefusedEmailTemplateId,
            iaAipFrontendUrl,
            oocDays,
            inCountryDays,
            recipientsFinder);
    }

    static Stream<Arguments> decisionScenarios() {
        return Stream.of(
            Arguments.of(Optional.of(FTPA_GRANTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_PARTIALLY_GRANTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_NOT_ADMITTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_REFUSED), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.of(FTPA_GRANTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_PARTIALLY_GRANTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_NOT_ADMITTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_REFUSED))
        );
    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_return_given_template_id_for_respondent_ftpa_decision(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);

        if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(respondentGrantedPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(respondentNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(respondentRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }
    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_return_given_template_id_for_appellant_ftpa_decision(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);

        if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)) {

            assertEquals(appellantGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(appellantPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(appellantNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(appellantRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }
    }

    @Test
    public void should_throw_error_if_ftpa_applicant_type_missing() {
        Mockito.when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaApplicantType is not present");
    }

    @Test
    public void should_throw_error_if_applicant_type_appellant_and_ftpa_appellant_decision_missing() {
        Mockito.when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        Mockito.when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaAppellantDecisionOutcomeType is not present");
    }

    @Test
    public void should_throw_error_if_applicant_type_respondent_and_ftpa_respondent_decision_missing() {
        Mockito.when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        Mockito.when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        Mockito.when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionOutcomeType is not present");
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS",
            appellantFtpaApplicationDecisionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_phone_number_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantFtpaApplicationDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_correctly_provide_appropriate_phrasing_when_decision_made(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        Set.of("respondent", "appellant")
            .forEach(applicantType -> {

                Mockito.when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(applicantType));

                if (applicantType.equals("respondent")) {
                    Mockito.when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    Mockito.when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                } else if (applicantType.equals("appellant")) {
                    Mockito.when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    Mockito.when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                }

                Map<String, String> personalisation =
                    appellantFtpaApplicationDecisionPersonalisationSms.getPersonalisation(asylumCase);

                if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)) {

                    assertEquals("granted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

                    assertEquals("partially granted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

                    assertEquals("not admitted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

                    assertEquals("refused", personalisation.get("applicationDecision"));
                }
            });
    }

    @Test
    public void should_return_personalisation_for_respondent_ftpa_decision() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FTPA_GRANTED));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(referenceNumber));
        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(referenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_for_appellant_ftpa_decision(YesOrNo appellantInUk) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FTPA_REFUSED));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(referenceNumber));
        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(referenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(appellantInUk.equals(YES)
            ? expectedDueDateInCountry
            : expectedDueDateOoc,
            personalisation.get("dueDate"));
    }
}
