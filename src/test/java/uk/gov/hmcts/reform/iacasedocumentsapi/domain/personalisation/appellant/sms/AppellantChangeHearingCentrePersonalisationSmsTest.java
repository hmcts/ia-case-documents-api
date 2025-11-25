package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeHearingCentrePersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    AsylumCase asylumCase;
    @Mock
    AsylumCase asylumCaseBefore;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    StringProvider stringProvider;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreName = "Taylor House";
    private final HearingCentre oldHearingCentre = HearingCentre.MANCHESTER;
    private final String oldHearingCentreName = "Manchester";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String removeAppealReason = "some remove appeal reason";

    private AppellantChangeHearingCentrePersonalisationSms appellantChangeHearingCentrePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCaseBefore.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(oldHearingCentre));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(stringProvider.get("hearingCentreName", oldHearingCentre.toString())).thenReturn(Optional.of(oldHearingCentreName));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreName));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(REMOVE_APPEAL_FROM_ONLINE_REASON, String.class))
                .thenReturn(Optional.of(removeAppealReason));

        appellantChangeHearingCentrePersonalisationSms = new AppellantChangeHearingCentrePersonalisationSms(
                smsTemplateId,
                recipientsFinder,
                stringProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantChangeHearingCentrePersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CHANGE_HEARING_CENTRE_AIP_APPELLANT_SMS",
                appellantChangeHearingCentrePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantChangeHearingCentrePersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantChangeHearingCentrePersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantChangeHearingCentrePersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                appellantChangeHearingCentrePersonalisationSms.getPersonalisation(callback);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(oldHearingCentreName, personalisation.get("oldHearingCentre"));
        assertEquals(hearingCentreName, personalisation.get("newHearingCentre"));
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMOVE_APPEAL_FROM_ONLINE_REASON, String.class))
                .thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantChangeHearingCentrePersonalisationSms.getPersonalisation(callback);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(oldHearingCentreName, personalisation.get("oldHearingCentre"));
        assertEquals(hearingCentreName, personalisation.get("newHearingCentre"));

    }
}
