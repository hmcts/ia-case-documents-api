package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms.AppellantEndAppealPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantEndAppealPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost/";
    private String iaAipFrontendPathToJudgeReview = "ask-judge-review";
    private String directLinkToJudgesReviewPage = "http://localhost/ask-judge-review";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantEndAppealPersonalisationSms appellantEndAppealPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantEndAppealPersonalisationSms = new AppellantEndAppealPersonalisationSms(
                smsTemplateId,
                iaAipFrontendUrl,
                iaAipFrontendPathToJudgeReview,
                recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantEndAppealPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_END_APPEAL_AIP_APPELLANT_SMS",
                appellantEndAppealPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantEndAppealPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantEndAppealPersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantEndAppealPersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String endAppealApprover = "Judge";
        String outcomeOfAppeal = "Withdrawn";
        String endAppealDate = LocalDate.now().toString();
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(endAppealApprover));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(outcomeOfAppeal));

        Map<String, String> personalisation =
                appellantEndAppealPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(directLinkToJudgesReviewPage, personalisation.get("direct link to judges’ review page"));
        assertEquals(endAppealApprover, personalisation.get("endAppealApprover"));
        assertEquals(LocalDate.parse(endAppealDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")), personalisation.get("endAppealDate"));
        assertEquals(outcomeOfAppeal, personalisation.get("outcomeOfAppeal"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantEndAppealPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(directLinkToJudgesReviewPage, personalisation.get("direct link to judges’ review page"));
    }
}
