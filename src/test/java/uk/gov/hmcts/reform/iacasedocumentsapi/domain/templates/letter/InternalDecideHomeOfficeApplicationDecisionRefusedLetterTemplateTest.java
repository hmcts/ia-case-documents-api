package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDecideHomeOfficeApplicationDecisionRefusedLetterTemplateTest {

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
    private InternalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate internalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate;

    @BeforeEach
    void setUp() {
        internalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate =
                new InternalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider,
                        makeAnApplicationService
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

        application.setDecisionReason("No reason");
        makeAnApplications.add(new IdValue<>("1", application));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));

        Map<String, String> applicationPropeties = Map.of(
                APPLICATION_TYPE, application.getType(),
                APPLICATION_DECISION, application.getDecision(),
                APPLICATION_DECISION_REASON, application.getDecisionReason()
        );

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(application));
        when(makeAnApplicationService.retrieveApplicationProperties(any())).thenReturn(applicationPropeties);
        when(makeAnApplicationService.getApplicationTypes(any())).thenReturn(MakeAnApplicationTypes.ADJOURN);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetup();

        Map<String, Object> templateFieldValues = internalDecideHomeOfficeApplicationDecisionRefusedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
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
    }

}
