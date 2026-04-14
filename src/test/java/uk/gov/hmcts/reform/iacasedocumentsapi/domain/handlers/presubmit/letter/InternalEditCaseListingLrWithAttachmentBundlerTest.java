package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
public class InternalEditCaseListingLrWithAttachmentBundlerTest {

    private InternalEditCaseListingLrWithAttachmentBundler internalEditCaseListingLrWithAttachmentBundler;
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

        internalEditCaseListingLrWithAttachmentBundler =
                new InternalEditCaseListingLrWithAttachmentBundler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING"})
    public void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(internalEditCaseListingLrWithAttachmentBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_incorrect_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(internalEditCaseListingLrWithAttachmentBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_incorrect_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(internalEditCaseListingLrWithAttachmentBundler.canHandle(stage, callback));
    }

    @Test
    public void it_should_not_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        internalEditCaseListingLrWithAttachmentBundler =
                new InternalEditCaseListingLrWithAttachmentBundler(
                        fileExtension,
                        fileName,
                        false,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);

        assertFalse(internalEditCaseListingLrWithAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_read_and_bundle_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> docID1 = new IdValue<>("1", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID2 = new IdValue<>("2", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID3 = new IdValue<>("3", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));
        IdValue<DocumentWithMetadata> docID4 = new IdValue<>("4", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEditCaseListingLrWithAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE)
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE)
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_detained_other_facility() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> docID1 = new IdValue<>("1", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID2 = new IdValue<>("2", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID3 = new IdValue<>("3", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));
        IdValue<DocumentWithMetadata> docID4 = new IdValue<>("4", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEditCaseListingLrWithAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE)
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE)
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_detained_prison_facility() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> docID1 = new IdValue<>("1", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID2 = new IdValue<>("2", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID3 = new IdValue<>("3", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));
        IdValue<DocumentWithMetadata> docID4 = new IdValue<>("4", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEditCaseListingLrWithAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE)
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE)
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_detained_other_facility_ooc() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> docID1 = new IdValue<>("1", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID2 = new IdValue<>("2", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LETTER));
        IdValue<DocumentWithMetadata> docID3 = new IdValue<>("3", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));
        IdValue<DocumentWithMetadata> docID4 = new IdValue<>("4", createDocumentWithMetadata(INTERNAL_EDIT_CASE_LISTING_LR_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalEditCaseListingLrWithAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE)
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE)
        );
    }

    @Test
    void set_to_late_dispatch() {
        assertThat(internalEditCaseListingLrWithAttachmentBundler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertThatThrownBy(() -> internalEditCaseListingLrWithAttachmentBundler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> internalEditCaseListingLrWithAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
                new Document("some-url",
                        "some-binary-url",
                        RandomStringUtils.secure().nextAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag) {

        return
                new DocumentWithMetadata(createDocumentWithDescription(),
                        RandomStringUtils.secure().nextAlphabetic(20),
                        new SystemDateProvider().now().toString(), documentTag,"test");

    }
}
