package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATEST;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalEditCaseListingPrisonAndIrcAttachmentBundlerTest {

    @Mock
    private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock
    private DocumentBundler documentBundler;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document bundleDocument;

    private String fileExtension = "PDF";
    private String fileName = "internal-edit-case-listing-letter-with-attachment";
    private boolean isEmStitchingEnabled = true;

    private InternalEditCaseListingPrisonAndIrcAttachmentBundler internalEditCaseListingPrisonAndIrcAttachmentBundler;

    @BeforeEach
    void setUp() {
        internalEditCaseListingPrisonAndIrcAttachmentBundler =
                new InternalEditCaseListingPrisonAndIrcAttachmentBundler(
                        fileExtension,
                        fileName,
                        isEmStitchingEnabled,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler
                );
    }

    @Test
    void should_have_late_dispatch_priority() {
        assertEquals(LATEST, internalEditCaseListingPrisonAndIrcAttachmentBundler.getDispatchPriority());
    }

    @Test
    void should_create_bundle_for_detained_in_prison_with_em_stitching_enabled() {
        List<DocumentWithMetadata> bundleDocuments = new ArrayList<>();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(fileNameQualifier.get(fileName + "." + fileExtension, caseDetails)).thenReturn("qualified-filename.PDF");
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("qualified-filename.PDF")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            bundleDocument,
            LETTER_BUNDLE_DOCUMENTS,
            DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE
        );

        verify(asylumCase, times(1)).clear(NOTIFICATION_ATTACHMENT_DOCUMENTS);
    }

    @Test
    void should_create_bundle_for_detained_in_irc_with_em_stitching_enabled() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(fileNameQualifier.get(fileName + "." + fileExtension, caseDetails)).thenReturn("qualified-filename.PDF");
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("qualified-filename.PDF")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            bundleDocument,
            LETTER_BUNDLE_DOCUMENTS,
            DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE
        );

        verify(asylumCase, times(1)).clear(NOTIFICATION_ATTACHMENT_DOCUMENTS);
    }

    @Test
    void should_handle_edit_case_listing_event_for_detained_in_prison_with_em_stitching_enabled() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @Test
    void should_handle_edit_case_listing_event_for_detained_in_irc_with_em_stitching_enabled() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @Test
    void should_not_handle_if_not_about_to_submit() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_not_edit_case_listing_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_em_stitching_disabled() {
        internalEditCaseListingPrisonAndIrcAttachmentBundler =
                new InternalEditCaseListingPrisonAndIrcAttachmentBundler(
                        fileExtension,
                        fileName,
                        false,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler
                );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_not_detained() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_detained_in_other_facility() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL_AUTOMATICALLY);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_cannot_handle_callback_for_all_events_and_stages_except_valid_ones() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event == Event.EDIT_CASE_LISTING) {
                    assertFalse(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalEditCaseListingPrisonAndIrcAttachmentBundler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
