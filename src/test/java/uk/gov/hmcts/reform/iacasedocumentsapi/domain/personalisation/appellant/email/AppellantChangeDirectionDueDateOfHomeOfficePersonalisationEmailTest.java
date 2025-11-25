package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    AppealService appealService;

    private Long caseId = 12345L;
    private String beforeListingEmailTemplateId = "beforeListingEmailTemplateId";
    private String afterListingEmailTemplateId = "afterListingEmailTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String ariaListingRef = "someAriaListingRef";

    private AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail;
    private String directionExplanation = "Some HO change direction due date content";
    private String dueDate = "2020-10-08";

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail =
                new AppellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail(
                        afterListingEmailTemplateId,
                        beforeListingEmailTemplateId,
                        personalisationProvider,
                        recipientsFinder,
                        appealService,
                        customerServicesProvider
                );
    }

    @Test
    public void should_return_given_template_id_for_before_listing() {
        assertEquals(beforeListingEmailTemplateId, appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_after_listing() {

        when(appealService.isAppealListed(asylumCase))
                .thenReturn(true);

        assertEquals(afterListingEmailTemplateId, appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE_EMAIL",
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_before_listing(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        initializePrefixes(appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback))
                .thenReturn(getPersonalisationForAppellant(mockedAppealReferenceNumber, mockedAppealHomeOfficeReferenceNumber, mockedAppellantGivenNames, mockedAppellantFamilyName));

        Map<String, String> personalisation =
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("dueDate"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given_after_listing(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        initializePrefixes(appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BELFAST));
        when(personalisationProvider.getPersonalisation(callback))
                .thenReturn(getPersonalisationForAppellant(mockedAppealReferenceNumber, mockedAppealHomeOfficeReferenceNumber, mockedAppellantGivenNames, mockedAppellantFamilyName));

        Map<String, String> personalisation =
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("dueDate"));
        assertEquals(ariaListingRef, personalisation.get("ariaListingReference"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        initializePrefixes(appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail);

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant("", "", "", ""));

        Map<String, String> personalisation =
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationEmail.getPersonalisation(callback);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("dueDate"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    private Map<String, String> getPersonalisationForAppellant(String mockedAppealReferenceNumber, String mockedAppealHomeOfficeReferenceNumber,
                                                               String mockedAppellantGivenNames, String mockedAppellantFamilyName) {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", mockedAppealReferenceNumber)
                .put("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
                .put("appellantGivenNames", mockedAppellantGivenNames)
                .put("appellantFamilyName", mockedAppellantFamilyName)
                .put("ariaListingReference", ariaListingRef)
                .put("explanation", directionExplanation)
                .put("dueDate", LocalDate
                        .parse(dueDate)
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                )
                .build();
    }

}
