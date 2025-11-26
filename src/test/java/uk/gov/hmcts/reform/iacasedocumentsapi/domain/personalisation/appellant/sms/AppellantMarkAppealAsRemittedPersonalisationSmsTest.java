package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SOURCE_OF_REMITTAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.REP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantMarkAppealAsRemittedPersonalisationSmsTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantMarkAppealAsRemittedPersonalisationSms
        appellantMarkAppealAsRemittedPersonalisationSms;
    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String templateId = "templateId";
    private String appellantMobileNumber = "07777777777";
    private SourceOfRemittal sourceOfRemittal = SourceOfRemittal.UPPER_TRIBUNAL;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(sourceOfRemittal));
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        appellantMarkAppealAsRemittedPersonalisationSms = new AppellantMarkAppealAsRemittedPersonalisationSms(
            templateId,
            recipientsFinder);
    }

    @Test
    public void should_return_given_email_address() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));

        assertEquals(Collections.singleton(appellantMobileNumber),
            appellantMarkAppealAsRemittedPersonalisationSms.getRecipientsList(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));

        assertEquals(Collections.emptySet(),
            appellantMarkAppealAsRemittedPersonalisationSms.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantMarkAppealAsRemittedPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_SMS",
            appellantMarkAppealAsRemittedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(sourceOfRemittal.getValue(), personalisation.get("remittalSource"));

    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        assertThatThrownBy(
            () -> appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_error_if_remittal_source_missing() {
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("sourceOfRemittal is not present");
    }

}
