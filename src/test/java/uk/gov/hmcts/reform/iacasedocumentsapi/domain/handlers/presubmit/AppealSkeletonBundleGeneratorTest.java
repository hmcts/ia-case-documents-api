package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSkeletonBundleGeneratorTest {

    private AppealSkeletonBundleGenerator appealSkeletonBundleGenerator;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock private DocumentBundler documentBundler;
    @Mock private DocumentHandler documentHandler;

    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private Document appealSkeletonBundle;

    @Before
    public void setUp() {

        String fileExtension = "PDF";
        String fileName = "some-file-name";

        appealSkeletonBundleGenerator =
            new AppealSkeletonBundleGenerator(
                fileExtension,
                fileName,
                fileNameQualifier,
                documentBundler,
                documentHandler
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");
        when(callback.getEvent()).thenReturn(Event.SUBMIT_CASE);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSkeletonBundleGenerator.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_call_document_bundler_with_correct_params_and_attach_to_case() {

        int expectedBundleSize = 1;
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

        assertThat(response.getData()).isEqualTo(asylumCase);

        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentHandler);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentHandler).addWithMetadata(any(AsylumCase.class), any(Document.class), any(AsylumCaseDefinition.class), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        assertThat(value.size()).isEqualTo(expectedBundleSize);

        assertThat(value).containsOnlyOnce(legalRepDoc2.getValue());
    }

    @Test
    public void should_call_document_bundler_with_when_there_are_no_documents() {

        int expectedBundleSize = 0;
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());

        when(documentBundler.bundle(
            anyList(),
            eq("Appeal skeleton documents"),
            eq("filename")
        )).thenReturn(appealSkeletonBundle);

        PreSubmitCallbackResponse<AsylumCase> response =
            appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getData()).isEqualTo(asylumCase);

        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentHandler);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentHandler).addWithMetadata(any(AsylumCase.class), any(Document.class), any(AsylumCaseDefinition.class), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        assertThat(value.size()).isEqualTo(expectedBundleSize);

        assertThat(value).isEmpty();

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> appealSkeletonBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
            new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag) {

        return
            new DocumentWithMetadata(createDocumentWithDescription(),
                RandomStringUtils.randomAlphabetic(20),
                new SystemDateProvider().now().toString(), documentTag);

    }
}