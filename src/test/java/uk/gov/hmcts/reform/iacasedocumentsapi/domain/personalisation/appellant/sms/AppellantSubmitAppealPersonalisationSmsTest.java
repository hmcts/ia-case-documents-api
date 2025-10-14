package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantSubmitAppealPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSubmitAppealPersonalisationSmsTest {

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

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String nonAipSmsTemplateId = "nonAipSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedLegalRepAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String dateOfBirth = "2020-03-01";
    private String expectedDateOfBirth = "1 Mar 2020";

    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";

    private AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);


        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedLegalRepAppealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppellantMobilePhone));

        appellantSubmitAppealPersonalisationSms = new AppellantSubmitAppealPersonalisationSms(
            smsTemplateId,
            nonAipSmsTemplateId,
            iaAipFrontendUrl,
            28,
            recipientsFinder,
            systemDateProvider,
            appealService);
    }

    @Test
    void should_return_given_template_id() {
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertEquals(smsTemplateId, appellantSubmitAppealPersonalisationSms.getTemplateId(asylumCase));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);
        assertEquals(nonAipSmsTemplateId, appellantSubmitAppealPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_SUBMITTED_APPELLANT_AIP_SMS",
            appellantSubmitAppealPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_recipients_when_mobile_is_empty() {

        when(asylumCase.read(MOBILE_NUMBER,String.class))
            .thenReturn(Optional.empty());
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantMobileNumber is not present");
    }


    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(true);
        assertTrue(
            appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_return_given_mobile_number_from_asylum_case_in_non_aip_case() {

        when(asylumCase.read(MOBILE_NUMBER))
            .thenReturn(Optional.of(mockedAppellantMobilePhone));
        when(appealService.isAppellantInPersonJourney(asylumCase)).thenReturn(false);

        assertTrue(
            appellantSubmitAppealPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantSubmitAppealPersonalisationSms.getPersonalisation(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant's birth of date is not present");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationSms.getPersonalisation(callback);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

        assertEquals("" + caseId, personalisation.get("Ref Number"));
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Legal Rep Ref"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));



    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {
        final String dueDate = LocalDate.now().plusDays(28)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(systemDateProvider.dueDate(28)).thenReturn(dueDate);


        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));

        Map<String, String> personalisation = appellantSubmitAppealPersonalisationSms.getPersonalisation(callback);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

        assertEquals("" + caseId, personalisation.get("Ref Number"));
        assertEquals("", personalisation.get("Legal Rep Ref"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));

    }
}
