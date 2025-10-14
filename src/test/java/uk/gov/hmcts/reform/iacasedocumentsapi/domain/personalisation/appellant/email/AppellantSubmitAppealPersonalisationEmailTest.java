package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSubmitAppealPersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;


    private Long caseId = 12345L;
    private String emailTemplateId = "someEmailTemplateId";
    private String emailWithoutHoReferenceTemplateId = "anotherEmailTemplateId";
    private String nonAipEmailTemplateId = "someEmailTemplateId1";
    private String iaAipFrontendUrl = "http://localhost";


    private String mockedLegalRepAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String dateOfBirth = "2020-03-01";
    private String expectedDateOfBirth = "1 Mar 2020";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedLegalRepAppealReferenceNumber));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(mockedAppellantEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantSubmitAppealPersonalisationEmail = new AppellantSubmitAppealPersonalisationEmail(
            emailTemplateId,
            emailWithoutHoReferenceTemplateId,
            nonAipEmailTemplateId,
            iaAipFrontendUrl,
            28,
            recipientsFinder,
            systemDateProvider,
            appealService,
            customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {

        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertEquals(emailTemplateId, appellantSubmitAppealPersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertEquals(emailWithoutHoReferenceTemplateId, appellantSubmitAppealPersonalisationEmail.getTemplateId(asylumCase));

        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);
        assertEquals(nonAipEmailTemplateId, appellantSubmitAppealPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP_EMAIL",
            appellantSubmitAppealPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);
        assertTrue(appellantSubmitAppealPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_return_given_email_address_in_asylum_case_in_non_aip_case() {

        when(asylumCase.read(EMAIL))
            .thenReturn(Optional.of(mockedAppellantEmailAddress));

        assertTrue(appellantSubmitAppealPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_throw_an_exception_email_address_empty() {
        when(asylumCase.read(EMAIL,String.class))
            .thenReturn(Optional.empty());
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);
        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationEmail.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantEmailAddress is not present");

    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationEmail.getPersonalisation(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant's birth of date is not present");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantSubmitAppealPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final String dueDate =
            LocalDate.now().plusDays(28)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationEmail.getPersonalisation(callback);

        assertEquals("" + caseId, personalisation.get("Ref Number"));
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedLegalRepAppealReferenceNumber, personalisation.get("Legal Rep Ref"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));

        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantSubmitAppealPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final String dueDate =
            LocalDate.now().plusDays(28)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));

        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationEmail.getPersonalisation(callback);

        assertEquals("" + caseId, personalisation.get("Ref Number"));
        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Legal Rep Ref"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
