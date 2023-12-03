package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppealSkeletonBundleGeneratorTest {

    private AppealSkeletonBundleGenerator appealSkeletonBundleGenerator;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock
    private DocumentBundler documentBundler;
    @Mock
    private DocumentHandler documentHandler;

    @Mock
    private DocumentWithMetadata documentWithMetadata;
    @Mock
    private Document appealSkeletonBundle;

    private String fileExtension = "PDF";
    private String fileName = "some-file-name";

    @BeforeEach
    public void setUp() {

        appealSkeletonBundleGenerator =
            new AppealSkeletonBundleGenerator(
                fileExtension,
                fileName,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    public void it_can_handle_callback(TestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());

        boolean canHandle = appealSkeletonBundleGenerator.canHandle(scenario.callbackStage, callback);

        assertEquals(canHandle, scenario.isExpected());
    }

    private static List<TestScenario> generateDifferentEventScenarios() {
        return TestScenario.builder();
    }


    @Value
    static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expected;

        public static List<TestScenario> builder() {
            List<TestScenario> testScenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.BUILD_CASE) || e.equals(Event.SUBMIT_CASE)) {
                    testScenarios.add(new TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new TestScenario(e, ABOUT_TO_SUBMIT, true));
                } else {
                    testScenarios.add(new TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new TestScenario(e, ABOUT_TO_SUBMIT, false));
                    testScenarios.add(new TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new TestScenario(e, ABOUT_TO_SUBMIT, false));
                }
            }
            return testScenarios;
        }
    }

    @Test
    public void it_should_not_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);

        appealSkeletonBundleGenerator =
            new AppealSkeletonBundleGenerator(
                fileExtension,
                fileName,
                false,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        boolean canHandle = appealSkeletonBundleGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    public void should_call_document_bundler_with_correct_params_and_attach_to_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");
        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);

        IdValue<DocumentWithMetadata> legalRepDoc1 = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION));
        IdValue<DocumentWithMetadata> legalRepDoc2 = new IdValue<>("2", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT));

        IdValue<DocumentWithMetadata> existingBundle = new IdValue<>("3", createDocumentWithMetadata(DocumentTag.APPEAL_SKELETON_BUNDLE));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(legalRepDoc1, legalRepDoc2, existingBundle)));

        when(documentBundler.bundle(
            anyList(),
            eq("Appeal skeleton documents"),
            eq("filename")
        )).thenReturn(appealSkeletonBundle);

        PreSubmitCallbackResponse<AsylumCase> response =
            appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(response.getData(), asylumCase);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentHandler);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentHandler).addWithMetadata(any(AsylumCase.class), any(Document.class), any(AsylumCaseDefinition.class), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        int expectedBundleSize = 1;
        assertEquals(value.size(), expectedBundleSize);

        assertThat(value).containsOnlyOnce(legalRepDoc2.getValue());
    }

    @Test
    public void should_call_document_bundler_with_when_there_are_no_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");
        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());

        when(documentBundler.bundle(
            anyList(),
            eq("Appeal skeleton documents"),
            eq("filename")
        )).thenReturn(appealSkeletonBundle);

        PreSubmitCallbackResponse<AsylumCase> response =
            appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(response.getData(), asylumCase);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentHandler);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentHandler).addWithMetadata(any(AsylumCase.class), any(Document.class), any(AsylumCaseDefinition.class), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        int expectedBundleSize = 0;
        assertEquals(value.size(), expectedBundleSize);

        assertThat(value).isEmpty();

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSkeletonBundleGenerator.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
            new Document(
                "some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20),
                "some-hash"
            );
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag) {

        return
            new DocumentWithMetadata(createDocumentWithDescription(),
                RandomStringUtils.randomAlphabetic(20),
                new SystemDateProvider().now().toString(), documentTag,"test");

    }
}
