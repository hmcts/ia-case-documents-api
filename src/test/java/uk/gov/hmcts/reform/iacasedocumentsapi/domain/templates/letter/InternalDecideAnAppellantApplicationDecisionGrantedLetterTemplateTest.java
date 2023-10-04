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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
public class InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplateTest {
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
    private final String templateName = "TB-IAC-DEC-ENG-00015.docx";
    private final String timeExtentionContent = "The Tribunal will give you more time to complete your next task. You will get a notification with the new date soon.";
    private final String adjournExpediteTransferOrUpdateHearingReqsContent = "The details of your hearing will be updated. The Tribunal will contact you when this happens.";
    private final String judgesReviewContent = "The decision on your original request will be overturned. The Tribunal will contact you if there is something you need to do next.";
    private final String linkOrUnlinkContent = "This appeal will be linked or unlinked. The Tribunal will contact you when this happens.";
    private final String withdrawnContent = "The Tribunal will end the appeal. The Tribunal will contact you when this happens.";
    private final String updateUpdateDetailsOrOtherContent = "The Tribunal will contact you when it makes the changes you requested.";
    private final String transferOutOfAdaContent = "Your appeal will continue but will no longer be decided within 25 working days. The Tribunal will change the date of your hearing. The Tribunal will contact you with a new date for your hearing and to tell you what will happen next with your appeal.";
    private static final String reinstateAppealContent = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";

    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1711";
    private final String internalAdaCustomerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplate internalDecideAnAppellantApplicationDecisionGrantedLetterTemplate;

    @BeforeEach
    void setUp() {
        internalDecideAnAppellantApplicationDecisionGrantedLetterTemplate =
                new InternalDecideAnAppellantApplicationDecisionGrantedLetterTemplate(
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
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDecideAnAppellantApplicationDecisionGrantedLetterTemplate.getName());
    }

    @ParameterizedTest
    @EnumSource(value = MakeAnApplicationTypes.class)
    void should_map_case_data_to_template_field_values(MakeAnApplicationTypes makeAnApplicationTypes) {
        dataSetup();
        List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
        final MakeAnApplication testApplication = new MakeAnApplication(
                "Admin Officer",
                makeAnApplicationTypes.getValue(),
                "someRandomDetails",
                new ArrayList<>(),
                LocalDate.now().toString(),
                "Granted",
                State.APPEAL_SUBMITTED.toString(),
                "caseworker-ia-admofficer");
        testApplication.setDecisionReason("No reason");
        makeAnApplications.add(new IdValue<>("1", testApplication));

        Map<String, String> applicationPropeties = Map.of(
                APPLICATION_TYPE, testApplication.getType(),
                APPLICATION_DECISION, testApplication.getDecision(),
                APPLICATION_DECISION_REASON, testApplication.getDecisionReason()
        );
        
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(testApplication));
        when(makeAnApplicationService.retrieveApplicationProperties(any())).thenReturn(applicationPropeties);
        when(makeAnApplicationService.getApplicationTypes(any())).thenReturn(makeAnApplicationTypes);

        Map<String, Object> templateFieldValues = internalDecideAnAppellantApplicationDecisionGrantedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(testApplication.getType(), templateFieldValues.get("applicationType"));
        assertEquals(testApplication.getDecisionReason(), templateFieldValues.get("applicationReason"));

        switch (makeAnApplicationTypes) {
            case TIME_EXTENSION -> assertEquals(timeExtentionContent, templateFieldValues.get("whatHappensNextContent"));
            case ADJOURN, EXPEDITE, TRANSFER, UPDATE_HEARING_REQUIREMENTS -> assertEquals(adjournExpediteTransferOrUpdateHearingReqsContent, templateFieldValues.get("whatHappensNextContent"));
            case JUDGE_REVIEW, JUDGE_REVIEW_LO -> assertEquals(judgesReviewContent, templateFieldValues.get("whatHappensNextContent"));
            case LINK_OR_UNLINK -> assertEquals(linkOrUnlinkContent, templateFieldValues.get("whatHappensNextContent"));
            case WITHDRAW -> assertEquals(withdrawnContent, templateFieldValues.get("whatHappensNextContent"));
            case REINSTATE -> assertEquals(reinstateAppealContent, templateFieldValues.get("whatHappensNextContent"));
            case UPDATE_APPEAL_DETAILS, OTHER -> assertEquals(updateUpdateDetailsOrOtherContent, templateFieldValues.get("whatHappensNextContent"));
            case TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS -> assertEquals(transferOutOfAdaContent, templateFieldValues.get("whatHappensNextContent"));
            default -> { }
        }
    }
}
