package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantMarkAsReadyForUtTransferPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantMarkAsReadyForUtTransferPersonalisationSmsTest {

    private final String appellantTemplateId = "TemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String utAppealReferenceNumber = "1234567890";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantMarkAsReadyForUtTransferPersonalisationSms appellantMarkAsReadyForUtTransferPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(utAppealReferenceNumber));

        appellantMarkAsReadyForUtTransferPersonalisationSms = new AppellantMarkAsReadyForUtTransferPersonalisationSms(
            appellantTemplateId,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(appellantTemplateId, appellantMarkAsReadyForUtTransferPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_APPELLANT_SMS",
            appellantMarkAsReadyForUtTransferPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String mobileNumber = "07781122334";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mobileNumber));

        assertTrue(appellantMarkAsReadyForUtTransferPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantMarkAsReadyForUtTransferPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantMarkAsReadyForUtTransferPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(utAppealReferenceNumber, personalisation.get("utAppealReferenceNumber"));

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantMarkAsReadyForUtTransferPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("utAppealReferenceNumber"));
    }

}
