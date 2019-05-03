package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingBundleGeneratorTest {

    private HearingBundleGenerator hearingBundleGenerator;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock private DocumentBundler documentBundler;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;

    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private Document hearingBundle;

    @Before
    public void setUp() {

        String fileExtension = "PDF";
        String fileName = "some-file-name";

        hearingBundleGenerator =
            new HearingBundleGenerator(
                fileExtension,
                fileName,
                fileNameQualifier,
                documentBundler,
                documentReceiver,
                documentsAppender
            );


        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");
        when(callback.getEvent()).thenReturn(Event.GENERATE_HEARING_BUNDLE);

    }


    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingBundleGenerator.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_HEARING_BUNDLE
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

        int expectedBundleSize = 3;
        IdValue<DocumentWithMetadata> legalRepDoc = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE));
        IdValue<DocumentWithMetadata> respondantDoc = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.APPEAL_RESPONSE));
        IdValue<DocumentWithMetadata> hearingDoc = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE));
        IdValue<DocumentWithMetadata> existingBundle = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_BUNDLE));

        when(asylumCase.getHearingDocuments()).thenReturn(Optional.of(Lists.newArrayList(hearingDoc, existingBundle)));
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.of(Lists.newArrayList(legalRepDoc)));
        when(asylumCase.getRespondentDocuments()).thenReturn(Optional.of(Lists.newArrayList(respondantDoc)));

        when(documentBundler.bundle(
            anyList(),
            eq("Hearing documents"),
            eq("filename")
        )).thenReturn(hearingBundle);

        when(documentReceiver.receive(
            eq(hearingBundle),
            eq(""),
            eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(documentWithMetadata);

        PreSubmitCallbackResponse<AsylumCase> response =
            hearingBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getData()).isEqualTo(asylumCase);

        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentReceiver, documentsAppender);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentReceiver).receive(any(Document.class), anyString(), any(DocumentTag.class));
        inOrder.verify(documentsAppender).append(anyList(), anyList(), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        assertThat(value.size()).isEqualTo(expectedBundleSize);

        assertThat(value).containsOnlyOnce(
            legalRepDoc.getValue(),
            respondantDoc.getValue(),
            hearingDoc.getValue()
        );
    }

    @Test
    public void should_call_document_bundler_with_when_there_are_no_documents() {

        int expectedBundleSize = 0;
        when(asylumCase.getHearingDocuments()).thenReturn(Optional.empty());
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.empty());
        when(asylumCase.getRespondentDocuments()).thenReturn(Optional.empty());

        when(documentBundler.bundle(
            anyList(),
            eq("Hearing documents"),
            eq("filename")
        )).thenReturn(hearingBundle);

        when(documentReceiver.receive(
            eq(hearingBundle),
            eq(""),
            eq(DocumentTag.HEARING_BUNDLE)
        )).thenReturn(documentWithMetadata);

        PreSubmitCallbackResponse<AsylumCase> response =
            hearingBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response.getData()).isEqualTo(asylumCase);

        ArgumentCaptor<List<DocumentWithMetadata>> captor = ArgumentCaptor.forClass(List.class);

        InOrder inOrder = inOrder(documentBundler, documentReceiver, documentsAppender);
        inOrder.verify(documentBundler).bundle(captor.capture(), anyString(), anyString());
        inOrder.verify(documentReceiver).receive(any(Document.class), anyString(), any(DocumentTag.class));
        inOrder.verify(documentsAppender).append(anyList(), anyList(), any(DocumentTag.class));

        List<DocumentWithMetadata> value = captor.getValue();
        assertThat(value.size()).isEqualTo(expectedBundleSize);

        assertThat(value).isEmpty();

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> hearingBundleGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
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
