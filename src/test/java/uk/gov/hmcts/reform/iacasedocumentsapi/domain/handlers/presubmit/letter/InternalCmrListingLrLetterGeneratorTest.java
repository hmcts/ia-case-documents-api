package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

class InternalCmrListingLrLetterGeneratorTest {

    private final DocumentCreator<AsylumCase> documentCreator = mock(DocumentCreator.class);
    private final DocumentHandler documentHandler = mock(DocumentHandler.class);

    private final Callback<AsylumCase> callback = mock(Callback.class);
    private final CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);
    private final AsylumCase asylumCase = mock(AsylumCase.class);

    private final Document document = mock(Document.class);

    private InternalCmrListingLrLetterGenerator handler;

    @BeforeEach
    void setUp() {

        handler = new InternalCmrListingLrLetterGenerator(
                documentCreator,
                documentHandler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        when(documentCreator.create(caseDetails)).thenReturn(document);
    }

    @Test
    void should_return_dispatch_priority() {

        assertEquals(DispatchPriority.EARLY, handler.getDispatchPriority());
    }

    @Test
    void should_handle_when_not_detained() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> isInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isAppellantInDetention(asylumCase))
                    .thenReturn(false);

            assertTrue(handler.canHandle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
            ));
        }
    }

    @Test
    void should_handle_when_detained_in_other() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> isInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isAppellantInDetention(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isDetainedInFacilityType(asylumCase, OTHER))
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
    void should_not_handle_when_not_internal_case() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> isInternalCase(asylumCase))
                    .thenReturn(false);

            assertFalse(handler.canHandle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
            ));
        }
    }

    @Test
    void should_not_handle_when_detained_not_other() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> isInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isAppellantInDetention(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isDetainedInFacilityType(asylumCase, OTHER))
                    .thenReturn(false);

            assertFalse(handler.canHandle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
            ));
        }
    }

    @Test
    void should_throw_for_null_stage() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> handler.canHandle(null, callback)
        );

        assertEquals("callbackStage must not be null", exception.getMessage());
    }

    @Test
    void should_throw_for_null_callback() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> handler.canHandle(
                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                        null
                )
        );

        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_when_cannot_handle() {

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(
                        PreSubmitCallbackStage.ABOUT_TO_START,
                        callback
                )
        );
    }

    @Test
    void should_create_letter_and_add_document() {

        try (MockedStatic<uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils> mocked =
                     mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> isInternalCase(asylumCase))
                    .thenReturn(true);

            mocked.when(() -> isAppellantInDetention(asylumCase))
                    .thenReturn(false);

            PreSubmitCallbackResponse<AsylumCase> response =
                    handler.handle(
                            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                            callback
                    );

            assertNotNull(response);

            verify(documentCreator).create(caseDetails);

            verify(documentHandler)
                .addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    document,
                    LETTER_NOTIFICATION_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER
            );
        }
    }
}
