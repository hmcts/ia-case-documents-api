package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;

import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantMakeAnApplicationPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String otherSmsTemplateId = "someOtherSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String applicationType = "someApplicationType";
    private String homeOfficeUser = "caseworker-ia-homeofficelart";
    private String citizenUser = "citizen";

    private AppellantMakeAnApplicationPersonalisationSms appellantMakeAnApplicationPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantMakeAnApplicationPersonalisationSms = new AppellantMakeAnApplicationPersonalisationSms(
                smsTemplateId,
            otherSmsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
                makeAnApplicationService,
                userDetailsProvider);
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false)).thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
    }

    @Test
    public void should_return_given_template_id() {
        when(userDetails.getRoles()).thenReturn(Arrays.asList(citizenUser));
        assertEquals(smsTemplateId, appellantMakeAnApplicationPersonalisationSms.getTemplateId());

        when(userDetails.getRoles()).thenReturn(Arrays.asList(homeOfficeUser));
        assertEquals(otherSmsTemplateId, appellantMakeAnApplicationPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_MAKE_AN_APPLICATION_APPELLANT_AIP_SMS",
            appellantMakeAnApplicationPersonalisationSms.getReferenceId(caseId));
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
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantMakeAnApplicationPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantMakeAnApplicationPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(applicationType, personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals("", personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }
}
