package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSmsTest {


    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private String recordOutOfDecisionCanProceedTemplateId = "recordOutOfDecisionCanProceedTemplateId";

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobileNumber = "1234445556";

    private AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms
            recordOutOfTimeDecisionCanProceedPersonalisationSms;

    @BeforeEach
    void setUp() {

        recordOutOfTimeDecisionCanProceedPersonalisationSms =
                new AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms(
                        recordOutOfDecisionCanProceedTemplateId,
                        iaAipFrontendUrl, recipientsFinder);

    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        Map<String, String> personalisation =
                recordOutOfTimeDecisionCanProceedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RECORD_OUT_OF_TIME_DECISION_CAN_PROCEED_AIP_SMS",
                recordOutOfTimeDecisionCanProceedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(recordOutOfTimeDecisionCanProceedPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> recordOutOfTimeDecisionCanProceedPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }
}
