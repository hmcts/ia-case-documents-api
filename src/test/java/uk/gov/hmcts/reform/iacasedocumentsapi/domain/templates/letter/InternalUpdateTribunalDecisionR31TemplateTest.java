package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalUpdateTribunalDecisionR31TemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    private InternalUpdateTribunalDecisionR31Template internalUpdateTribunalDecisionR31Template;
    private final String templateName = "TB-IAC-LET-ENG-00050.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final LocalDate now = LocalDate.now();
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String sendDecisionAndReasonDate = "2023-09-28";
    private final LocalDate currentDate = LocalDate.now();
    private final Document coverLetterDocument = mock(Document.class);
    private final Document documentAndReasonsDocument = mock(Document.class);

    @BeforeEach
    void setUp() {
        internalUpdateTribunalDecisionR31Template =
            new InternalUpdateTribunalDecisionR31Template(
                templateName,
                customerServicesProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, internalUpdateTribunalDecisionR31Template.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(SEND_DECISIONS_AND_REASONS_DATE, String.class)).thenReturn(Optional.of(sendDecisionAndReasonDate));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalUpdateTribunalDecisionR31Template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_throw_exception_when_original_date_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(SEND_DECISIONS_AND_REASONS_DATE, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> internalUpdateTribunalDecisionR31Template.mapFieldValues(caseDetails)
        );

        assertEquals("Send Decisions and reasons date due is not present", exception.getMessage());
    }

    @Test
    void should_handle_first_check_only() {
        dataSetUp();

        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));

        Map<String, Object> templateFieldValues = internalUpdateTribunalDecisionR31Template.mapFieldValues(caseDetails);

        String dynamicContent = (String) templateFieldValues.get("dynamicContentBasedOnDecision");

        assertEquals("The Tribunal made a mistake recording your appeal decision. \n\n Your decision was recorded as allowed but should have been recorded as dismissed. \n\n The Tribunal has fixed this mistake and your appeal decision has been correctly recorded as dismissed. \n\n If you disagree with the appeal decision, you have until 12 October 2023 to ask for permission to appeal to the Upper Tribunal.", dynamicContent);
    }

    @Test
    void should_handle_second_check_only() {
        dataSetUp();

        final DynamicList dynamicList = new DynamicList(
            new Value("dismissed", "No"),
            newArrayList()
        );

        List<IdValue<DecisionAndReasons>> correctedDecAndReasonMock =
            List.of(
                new IdValue<>("1", DecisionAndReasons.builder()
                    .updatedDecisionDate(currentDate.toString())
                    .dateCoverLetterDocumentUploaded("2024-08-16")
                    .coverLetterDocument(coverLetterDocument)
                    .dateDocumentAndReasonsDocumentUploaded("2024-08-16")
                    .documentAndReasonsDocument(documentAndReasonsDocument)
                    .summariseChanges("some changes")
                    .build())
            );

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));
        when(asylumCase.read(CORRECTED_DECISION_AND_REASONS)).thenReturn(Optional.of(correctedDecAndReasonMock));

        Map<String, Object> templateFieldValues = internalUpdateTribunalDecisionR31Template.mapFieldValues(caseDetails);

        String dynamicContent = (String) templateFieldValues.get("dynamicContentBasedOnDecision");
        assertEquals("The Tribunal entered some wrong information in the Decision and Reasons document for this appeal. \n\n The Tribunal has created a <b>new Decisions and Reasons document</b> which includes the correct information. This document should be given to you along with this letter. ", dynamicContent);
    }

    @Test
    void should_handle_third_check_only() {
        dataSetUp();

        final DynamicList dynamicList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
        );

        List<IdValue<DecisionAndReasons>> correctedDecAndReasonMock =
            List.of(
                new IdValue<>("1", DecisionAndReasons.builder()
                    .updatedDecisionDate(currentDate.toString())
                    .dateCoverLetterDocumentUploaded("2024-08-16")
                    .coverLetterDocument(coverLetterDocument)
                    .dateDocumentAndReasonsDocumentUploaded("2024-08-16")
                    .documentAndReasonsDocument(documentAndReasonsDocument)
                    .summariseChanges("some changes")
                    .build())
            );

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));
        when(asylumCase.read(CORRECTED_DECISION_AND_REASONS)).thenReturn(Optional.of(correctedDecAndReasonMock));

        Map<String, Object> templateFieldValues = internalUpdateTribunalDecisionR31Template.mapFieldValues(caseDetails);

        String dynamicContent = (String) templateFieldValues.get("dynamicContentBasedOnDecision");
        assertEquals("The Tribunal made a mistake recording your appeal decision. \n\n Your decision was recorded as allowed but should have been recorded as dismissed. \n\n The Tribunal has fixed this mistake and your appeal decision has been correctly recorded as dismissed.\n\n The Tribunal also entered some wrong information in the Decision and Reasons document for this appeal. \n\n The Tribunal has created a <b>new Decisions and Reasons document</b> which includes the correct information. This document should be given to you along with this letter. \n\n If you disagree with the appeal decision, you have until 30 August 2024 to ask for permission to appeal to the Upper Tribunal. ", dynamicContent);
    }
}