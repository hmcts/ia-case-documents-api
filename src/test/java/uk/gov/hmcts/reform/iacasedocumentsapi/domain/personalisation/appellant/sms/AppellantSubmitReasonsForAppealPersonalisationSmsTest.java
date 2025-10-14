package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantSubmitReasonsForAppealPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantSubmitReasonsForAppealPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantSubmitReasonsForAppealPersonalisationSms appellantReasonsForAppealSubmittedPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantReasonsForAppealSubmittedPersonalisationSms = new AppellantSubmitReasonsForAppealPersonalisationSms(
            smsTemplateId,
            iaAipFrontendUrl,
            14,
            recipientsFinder,
            systemDateProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        Assert.assertEquals(smsTemplateId, appellantReasonsForAppealSubmittedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Assert.assertEquals(caseId + "_SUBMIT_REASONS_FOR_APPEAL_APPELLANT_AIP_SMS",
            appellantReasonsForAppealSubmittedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantReasonsForAppealSubmittedPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantReasonsForAppealSubmittedPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantReasonsForAppealSubmittedPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantReasonsForAppealSubmittedPersonalisationSms.getPersonalisation(asylumCase);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {
        final String dueDate = LocalDate.now().plusDays(14)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantReasonsForAppealSubmittedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(dueDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }
}
