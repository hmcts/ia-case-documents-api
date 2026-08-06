package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

class InternalCmrListingLegalRepLetterBundlerTest {

    private final FileNameQualifier<AsylumCase> fileNameQualifier = mock(FileNameQualifier.class);
    private final DocumentBundler documentBundler = mock(DocumentBundler.class);
    private final DocumentHandler documentHandler = mock(DocumentHandler.class);

    private final Callback<AsylumCase> callback = mock(Callback.class);
    private final CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);
    private final AsylumCase asylumCase = mock(AsylumCase.class);

    private final Document bundledDocument = mock(Document.class);

    private InternalCmrListingLegalRepLetterBundler handler;

    @BeforeEach
    void setUp() {

        handler = new InternalCmrListingLegalRepLetterBundler(
                "pdf",
                "internal-letter",
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        when(fileNameQualifier.get(anyString(), eq(caseDetails)))
                .thenReturn("qualified.pdf");

        when(documentBundler.bundleWithoutContentsOrCoverSheets(anyList(), anyString(), anyString()))
                .thenReturn(bundledDocument);

        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_return_dispatch_priority() {

        assertEquals(DispatchPriority.LATE, handler.getDispatchPriority());
    }

    @Test
    void should_handle() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
                    .thenReturn(true);

            assertTrue(handler.canHandle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
            ));
        }
    }

    @Test
    void should_not_handle_wrong_stage() {

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
        ));
    }

    @Test
    void should_not_handle_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertFalse(handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        ));
    }

    @Test
    void should_throw_for_null_stage() {

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> handler.canHandle(null, callback)
        );

        assertEquals("callbackStage must not be null", ex.getMessage());
    }

    @Test
    void should_throw_for_null_callback() {

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null)
        );

        assertEquals("callback must not be null", ex.getMessage());
    }

    @Test
    void should_throw_if_cannot_handle() {

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(
                        PreSubmitCallbackStage.ABOUT_TO_START,
                        callback
                )
        );
    }

    @Test
    void should_bundle_non_detained_case() {

        List<DocumentWithMetadata> lrDocs = List.of(mock(DocumentWithMetadata.class));

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isDetainedInOneOfFacilityTypes(asylumCase, PRISON, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC))
                    .thenReturn(false);

            mocked.when(() ->
                            getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER))
                    .thenReturn(lrDocs);

            PreSubmitCallbackResponse<AsylumCase> response =
                    handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(response);

            // both futures read the same tag, so the same documents are bundled twice
            verify(documentBundler, times(2)).bundleWithoutContentsOrCoverSheets(
                    eq(lrDocs),
                    eq("Letter bundle documents"),
                    eq("qualified.pdf")
            );

            verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    bundledDocument,
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE
            );

            verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    bundledDocument,
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER_BUNDLE
            );
        }
    }

    @Test
    void should_qualify_bundle_file_name_from_injected_file_name_and_extension() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
                    .thenReturn(true);

            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(fileNameQualifier).get("internal-letter.pdf", caseDetails);

            verify(documentBundler, times(2)).bundleWithoutContentsOrCoverSheets(
                    anyList(),
                    eq("Letter bundle documents"),
                    eq("qualified.pdf")
            );
        }
    }

    @Test
    void should_bundle_detained_case() {

        List<DocumentWithMetadata> attachmentDocs = List.of(mock(DocumentWithMetadata.class));
        List<DocumentWithMetadata> lrDocs = List.of(mock(DocumentWithMetadata.class));

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isDetainedInOneOfFacilityTypes(asylumCase, PRISON, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC))
                    .thenReturn(true);

            mocked.when(() ->
                            getMaybeNotificationAttachmentDocuments(asylumCase, DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER))
                    .thenReturn(attachmentDocs);

            mocked.when(() ->
                            getMaybeLetterNotificationDocuments(asylumCase, DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER))
                    .thenReturn(lrDocs);

            handler.handle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
            );

            verify(documentBundler).bundleWithoutContentsOrCoverSheets(
                    eq(attachmentDocs),
                    eq("Letter bundle documents"),
                    eq("qualified.pdf")
            );

            verify(documentBundler).bundleWithoutContentsOrCoverSheets(
                    eq(lrDocs),
                    eq("Letter bundle documents"),
                    eq("qualified.pdf")
            );

            verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    bundledDocument,
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE
            );

            verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    bundledDocument,
                    LETTER_BUNDLE_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER_BUNDLE
            );
        }
    }
}
