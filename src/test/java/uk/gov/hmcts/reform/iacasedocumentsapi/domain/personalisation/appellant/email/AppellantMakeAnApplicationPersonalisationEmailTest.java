package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantMakeAnApplicationPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    AppealService appealService;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    @Mock
    MakeAnApplication makeAnApplication;

    private Long caseId = 12345L;
    private String beforeListingEmailTemplateId = "beforeListingEmailTemplateId";
    private String afterListingEmailTemplateId = "afterListingEmailtemplateId";
    private String otherBeforeListingEmailTemplateId = "otherBeforeListingEmailTemplateId";
    private String otherAfterListingEmailTemplateId = "otherAfterListingEmailtemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String applicationType = "someApplicationType";
    private String applicationTypePhrase = "some application type";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String ariaListingReference = "someReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private final String homeOfficeUser = "caseworker-ia-homeofficelart";
    private final String citizenUser = "citizen";

    private AppellantMakeAnApplicationPersonalisationEmail appellantMakeAnApplicationPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false)).thenReturn(Optional.of(makeAnApplication));
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        appellantMakeAnApplicationPersonalisationEmail = new AppellantMakeAnApplicationPersonalisationEmail(
            beforeListingEmailTemplateId,
            afterListingEmailTemplateId,
            otherBeforeListingEmailTemplateId,
            otherAfterListingEmailTemplateId,
            iaAipFrontendUrl,
            recipientsFinder, appealService, makeAnApplicationService, userDetailsProvider);
    }

    @Test
    public void should_return_given_template_id() {
        when(userDetails.getRoles()).thenReturn(Arrays.asList(citizenUser));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(beforeListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(userDetails.getRoles()).thenReturn(Arrays.asList(homeOfficeUser));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(otherBeforeListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(otherAfterListingEmailTemplateId, appellantMakeAnApplicationPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_MAKE_AN_APPLICATION_APPELLANT_AIP_EMAIL",
            appellantMakeAnApplicationPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantMakeAnApplicationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantMakeAnApplicationPersonalisationEmail.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = { citizenUser, homeOfficeUser })
    public void should_return_personalisation_when_all_information_given(String user) {
        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(userDetails.getRoles()).thenReturn(List.of(user));
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false))
            .thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplicationService.mapApplicationTypeToPhrase(makeAnApplication))
            .thenReturn(applicationTypePhrase);

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(user.equals(citizenUser) ? applicationType : applicationTypePhrase,
            personalisation.get("applicationType"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("HO Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantMakeAnApplicationPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final String dueDate =
            LocalDate.now().plusDays(28)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(makeAnApplication.getType()).thenReturn("");

        Map<String, String> personalisation =
            appellantMakeAnApplicationPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("HO Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals("", personalisation.get("applicationType"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

        verify(makeAnApplicationService).getMakeAnApplication(asylumCase, false);
    }
}
