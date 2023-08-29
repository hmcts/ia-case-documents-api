package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDecideAnApplicationDecisionRefusedLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private MakeAnApplicationService makeAnApplicationService;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    private final String judgeRole = "caseworker-ia-iacjudge";
    private final String legalOfficerRole = "caseworker-ia-caseofficer";
    private final String applicationRefusedAdaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private final String applicationRefusedDetainedNonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
    private final String templateName = "TB-IAC-DEC-ENG-00016.docx";
    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1711";
    private final String internalAdaCustomerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
    private final MakeAnApplication application = new MakeAnApplication(
            "Admin Officer",
            "Adjourn",
            "someRandomDetails",
            new ArrayList<>(),
            LocalDate.now().toString(),
            "Refused",
            State.APPEAL_SUBMITTED.toString(),
            "caseworker-ia-admofficer");
    private InternalDecideAnApplicationDecisionRefusedLetterTemplate internalDecideAnApplicationDecisionRefusedLetterTemplate;

    @BeforeEach
    void setUp() {
        internalDecideAnApplicationDecisionRefusedLetterTemplate =
                new InternalDecideAnApplicationDecisionRefusedLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider,
                        makeAnApplicationService,
                        userDetailsProvider
                );
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(internalAdaCustomerServicesEmailAddress);

        when(dateProvider.now()).thenReturn(LocalDate.now());

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of(judgeRole));

        makeAnApplications.add(new IdValue<>("1", application));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(application));
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDecideAnApplicationDecisionRefusedLetterTemplate.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {judgeRole, legalOfficerRole})
    void should_map_case_data_to_template_field_values_and_map_decision_maker_field_correctly(String role) {
        dataSetup();

        when(userDetailsProvider.getUserDetails().getRoles()).thenReturn(List.of(role));

        Map<String, Object> templateFieldValues = internalDecideAnApplicationDecisionRefusedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(application.getType(), templateFieldValues.get("applicationType"));
        assertEquals(application.getDecisionReason(), templateFieldValues.get("applicationReason"));
        assertEquals(applicationRefusedDetainedNonAdaFormName, templateFieldValues.get("formName"));

        if (role.equals(judgeRole)) {
            assertEquals("Judge", templateFieldValues.get("decisionMaker"));
        } else if (role.equals(legalOfficerRole)) {
            assertEquals("Legal Officer", templateFieldValues.get("decisionMaker"));
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_map_case_data_to_template_field_values_and_map_decision_maker_field_correctly(YesOrNo yesOrNo) {
        dataSetup();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));
        Map<String, Object> templateFieldValues = internalDecideAnApplicationDecisionRefusedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(application.getType(), templateFieldValues.get("applicationType"));
        assertEquals(application.getDecisionReason(), templateFieldValues.get("applicationReason"));
        assertEquals("Judge", templateFieldValues.get("decisionMaker"));


        if (yesOrNo.equals(YesOrNo.YES)) {
            assertEquals(applicationRefusedAdaFormName, templateFieldValues.get("formName"));
        } else {
            assertEquals(applicationRefusedDetainedNonAdaFormName, templateFieldValues.get("formName"));
        }
    }

    @Test
    void should_throw_exception_if_application_type_cannot_be_parsed() {
        dataSetup();
        List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
        final MakeAnApplication testApplication = new MakeAnApplication(
                "Admin Officer",
                "someRandomApplicationTypeThatShouldCauseAnException",
                "someRandomDetails",
                new ArrayList<>(),
                LocalDate.now().toString(),
                "Granted",
                State.APPEAL_SUBMITTED.toString(),
                "caseworker-ia-admofficer");
        makeAnApplications.add(new IdValue<>("1", testApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(testApplication));

        assertThatThrownBy(() -> internalDecideAnApplicationDecisionRefusedLetterTemplate.mapFieldValues(caseDetails))
                .hasMessage("Application type could not be parsed")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}
