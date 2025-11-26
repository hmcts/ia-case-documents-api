package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalEndAppealDetWithAttachmentBundleHandlerTest {

    private InternalEndAppealDetWithAttachmentBundleHandler internalEndAppealDetWithAttachmentBundleHandler;
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
    private Document bundleDocument;
    private String fileExtension = "PDF";
    private String fileName = "some-file-name";

    @BeforeEach
    public void setUp() {

        internalEndAppealDetWithAttachmentBundleHandler =
                new InternalEndAppealDetWithAttachmentBundleHandler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);
    }

    @Test
    public void it_should_not_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        internalEndAppealDetWithAttachmentBundleHandler =
                new InternalEndAppealDetWithAttachmentBundleHandler(
                        fileExtension,
                        fileName,
                        false,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);

        boolean canHandle = internalEndAppealDetWithAttachmentBundleHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_detained_irc() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(callback.getCaseDetails().getCaseData().read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEndAppealDetWithAttachmentBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase, bundleDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.END_APPEAL
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_detained_prison() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(callback.getCaseDetails().getCaseData().read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEndAppealDetWithAttachmentBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase, bundleDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.END_APPEAL
        );
    }

    @Test
    void set_to_late_dispatch() {
        assertThat(internalEndAppealDetWithAttachmentBundleHandler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertThatThrownBy(() -> internalEndAppealDetWithAttachmentBundleHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> internalEndAppealDetWithAttachmentBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
                new Document("some-url",
                        "some-binary-url",
                        RandomStringUtils.secure().nextAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata() {

        return
                new DocumentWithMetadata(createDocumentWithDescription(),
                        RandomStringUtils.secure().nextAlphabetic(20),
                        new SystemDateProvider().now().toString(), DocumentTag.INTERNAL_END_APPEAL_LETTER,"test");

    }
}
