package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class AppellantFtpaApplicationDecisionPersonalisationEmailTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String respondentGrantedPartiallyGrantedEmailTemplateId = "respondentGrantedPartiallyGrantedEmailTemplateId";
    private String respondentNotAdmittedEmailTemplateId = "respondentNotAdmittedEmailTemplateId";
    private String respondentRefusedEmailTemplateId = "respondentRefusedEmailTemplateId";
    private String appellantGrantedEmailTemplateId = "appellantGrantedEmailTemplateId";
    private String appellantPartiallyGrantedEmailTemplateId = "appellantPartiallyGrantedEmailTemplateId";
    private String appellantNotAdmittedEmailTemplateId = "appellantNotAdmittedEmailTemplateId";
    private String appellantRefusedEmailTemplateId = "appellantRefusedEmailTemplateId";


    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String mockedAppellantEmail = "fake@faketest.com";
    private String mockedAriaListingReferenceNumber = "ariaListingReferenceNumber";

    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private long oocDays = 28;
    private long inCountryDays = 14;
    private LocalDate today = LocalDate.now();
    private String expectedDueDateOoc = today.plusDays(oocDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    private String expectedDueDateInCountry = today.plusDays(inCountryDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

    private AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReferenceNumber));
        when((customerServicesProvider.getCustomerServicesPersonalisation())).thenReturn(
            Map.of(
                "customerServicesTelephone", customerServicesTelephone,
                "customerServicesEmail", customerServicesEmail
            ));

        appellantFtpaApplicationDecisionPersonalisationEmail = new AppellantFtpaApplicationDecisionPersonalisationEmail(
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
            recipientsFinder,
            customerServicesProvider);

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

            assertEquals(respondentGrantedPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(respondentNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(respondentRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
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

            assertEquals(appellantGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(appellantPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(appellantNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(appellantRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }
    }

    @Test
    public void should_throw_error_if_ftpa_applicant_type_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaApplicantType is not present");
    }

    @Test
    public void should_throw_error_if_applicant_type_appellant_and_ftpa_appellant_decision_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("appellant"));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaAppellantDecisionOutcomeType is not present");
    }

    @Test
    public void should_throw_error_if_applicant_type_respondent_and_ftpa_respondent_decision_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionOutcomeType is not present");
    }

    @Test
    public void should_throw_error_if_applicant_type_is_neither_respondent_nor_appellant() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("thirdOption")); //not allowed applicant type
        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("applicantType not of type appellant or respondent");
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_EMAIL",
            appellantFtpaApplicationDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            mockedAppellantEmail, //email
            YES, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantFtpaApplicationDecisionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantFtpaApplicationDecisionPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo appellantInUk) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_GRANTED));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));

        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals("\nListing reference: " + mockedAriaListingReferenceNumber,
            personalisation.get("listingReferenceLine"));
        assertNotNull(personalisation.get("applicationDecision"));
        assertEquals(appellantInUk.equals(YES)
            ? expectedDueDateInCountry
            : expectedDueDateOoc,
            personalisation.get("dueDate"));

    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_correctly_provide_appropriate_phrasing_when_decision_made(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        Set.of("respondent", "appellant")
            .forEach(applicantType -> {

                when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(applicantType));

                if (applicantType.equals("respondent")) {
                    when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                } else if (applicantType.equals("appellant")) {
                    when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                }

                Map<String, String> personalisation =
                    appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

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
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_GRANTED));

        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals("granted", personalisation.get("applicationDecision"));
        assertEquals("", personalisation.get("dueDate"));
    }
}
