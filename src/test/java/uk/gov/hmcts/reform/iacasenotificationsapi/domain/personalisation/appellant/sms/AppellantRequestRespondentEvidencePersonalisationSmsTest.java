package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@RunWith(MockitoJUnitRunner.class)
public class AppellantRequestRespondentEvidencePersonalisationSmsTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock RecipientsFinder recipientsFinder;
    @Mock Direction direction;

    private Long caseId = 12345L;
    private String emailTemplateId = "someEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String directionDueDate = "2019-08-27";
    private String expectedDirectionDueDate = "27 Aug 2019";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantRequestRespondentEvidencePersonalisationSms appellantRequestRespondentEvidencePersonalisationSms;

    @Before
    public void setup() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantRequestRespondentEvidencePersonalisationSms = new AppellantRequestRespondentEvidencePersonalisationSms(
            emailTemplateId,
            iaAipFrontendUrl,
            directionFinder,
            recipientsFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantRequestRespondentEvidencePersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REQUEST_RESPONDENT_EVIDENCE_DIRECTION_APPELLANT_AIP_SMS", appellantRequestRespondentEvidencePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YesOrNo.YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantRequestRespondentEvidencePersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantRequestRespondentEvidencePersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantRequestRespondentEvidencePersonalisationSms.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("direction 'respondentEvidence' is not present");
    }


    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantRequestRespondentEvidencePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(expectedDirectionDueDate, personalisation.get("direction due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = appellantRequestRespondentEvidencePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(expectedDirectionDueDate, personalisation.get("direction due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
    }
}
