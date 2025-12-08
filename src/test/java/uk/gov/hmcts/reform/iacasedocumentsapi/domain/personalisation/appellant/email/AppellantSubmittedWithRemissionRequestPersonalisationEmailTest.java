package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantSubmittedWithRemissionRequestPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private final Long caseId = 12345L;
    private final String emailTemplateId = "someEmailTemplateId";
    private final String paPayLaterEmailTemplateId = "paPayLaterEmailTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String mockedAppellantEmailAddress = "appelant@example.net";

    private AppellantSubmittedWithRemissionRequestPersonalisationEmail appellantSubmittedWithRemissionRequestPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        appellantSubmittedWithRemissionRequestPersonalisationEmail =
            new AppellantSubmittedWithRemissionRequestPersonalisationEmail(
                emailTemplateId,
                paPayLaterEmailTemplateId,
                14,
                iaAipFrontendUrl,
                14,
                recipientsFinder,
                systemDateProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantSubmittedWithRemissionRequestPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_pa_pay_later_template_id_when_pa_and_payLater() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertEquals(paPayLaterEmailTemplateId, appellantSubmittedWithRemissionRequestPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_pa_pay_later_template_id_when_pa_and_payOffline() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(paPayLaterEmailTemplateId, appellantSubmittedWithRemissionRequestPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_default_template_id_when_pa_and_payment_option_unknown() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("someOtherOption"));

        assertEquals(emailTemplateId, appellantSubmittedWithRemissionRequestPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_default_template_id_when_appeal_type_not_pa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        assertEquals(emailTemplateId, appellantSubmittedWithRemissionRequestPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_EMAIL", appellantSubmittedWithRemissionRequestPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantSubmittedWithRemissionRequestPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantSubmittedWithRemissionRequestPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSubmittedWithRemissionRequestPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(dueDate, personalisation.get("appealSubmittedDaysAfter"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSubmittedWithRemissionRequestPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(dueDate, personalisation.get("appealSubmittedDaysAfter"));
    }

    @Test
    public void should_return_pa_personalisation_when_all_info_present() {
        final String refundDueDate = LocalDate.now().plusDays(14)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(14)).thenReturn(refundDueDate);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        Map<String, String> personalisation = appellantSubmittedWithRemissionRequestPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(refundDueDate, personalisation.get("14 days after remission request sent"));
    }

    @Test
    public void should_return_pa_personalisation_with_missing_fields() {
        final String refundDueDate = LocalDate.now().plusDays(14)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(14)).thenReturn(refundDueDate);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        Map<String, String> personalisation = appellantSubmittedWithRemissionRequestPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(refundDueDate, personalisation.get("14 days after remission request sent"));
    }
}
