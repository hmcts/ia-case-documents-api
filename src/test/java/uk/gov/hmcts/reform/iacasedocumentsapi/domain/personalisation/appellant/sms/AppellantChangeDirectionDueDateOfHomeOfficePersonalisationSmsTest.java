package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String smsTemplateId = "afterListingEmailTemplateId";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms;
    private String directionExplanation = "Some HO change direction due date content";
    private String dueDate = "2020-10-08";

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms =
                new AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms(
                        smsTemplateId,
                        personalisationProvider,
                        recipientsFinder
                );
    }

    @Test
    public void should_return_given_template_id_for_after_listing() {

        assertEquals(smsTemplateId, appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE_SMS",
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant());

        Map<String, String> personalisation =
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals("8 Oct 2020", personalisation.get("dueDate"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant());

        Map<String, String> personalisation =
                appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getPersonalisation(callback);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("8 Oct 2020", personalisation.get("dueDate"));
        assertEquals(directionExplanation, personalisation.get("explanation"));
    }

    private Map<String, String> getPersonalisationForAppellant() {
        return ImmutableMap
                .<String, String>builder()
                .put("explanation", directionExplanation)
                .put("dueDate", LocalDate
                        .parse(dueDate)
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
)
                .build();
    }

}
