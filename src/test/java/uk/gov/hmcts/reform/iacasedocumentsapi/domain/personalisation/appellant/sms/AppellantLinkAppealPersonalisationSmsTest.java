package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REASON_FOR_LINK_APPEAL;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantLinkAppealPersonalisationSms;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantLinkAppealPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantLinkAppealPersonalisationSms appellantLinkAppealPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class))
                .thenReturn(Optional.of(ReasonForLinkAppealOptions.FAMILIAL));

        appellantLinkAppealPersonalisationSms = new AppellantLinkAppealPersonalisationSms(
                smsTemplateId,
                recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantLinkAppealPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LINK_APPEAL_AIP_APPELLANT_SMS",
                appellantLinkAppealPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantLinkAppealPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantLinkAppealPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantLinkAppealPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                appellantLinkAppealPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(ReasonForLinkAppealOptions.FAMILIAL.getId(), personalisation.get("reason"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class))
                .thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantLinkAppealPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("reason"));
    }
}
