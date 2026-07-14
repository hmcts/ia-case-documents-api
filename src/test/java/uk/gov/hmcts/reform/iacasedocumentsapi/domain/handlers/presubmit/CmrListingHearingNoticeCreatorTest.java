package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CmrListingHearingNoticeCreatorTest {

    @Mock private DocumentCreator<AsylumCase> cmrHearingNoticeDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> adaCmrHearingNoticeDocumentCreator;
    @Mock private DocumentHandler documentHandler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;

    private CmrListingHearingNoticeCreator cmrListingHearingNoticeCreator;

    @BeforeEach
    public void setUp() {

        cmrListingHearingNoticeCreator =
            new CmrListingHearingNoticeCreator(
                cmrHearingNoticeDocumentCreator,
                remoteCmrHearingNoticeDocumentCreator,
                adaCmrHearingNoticeDocumentCreator,
                documentHandler
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CMR_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
    }

    @Test
    void should_create_cmr_hearing_notice_and_append_to_hearing_documents() {

        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(cmrHearingNoticeDocumentCreator, times(1)).create(caseDetails);
        verify(remoteCmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(adaCmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER);
    }

    @Test
    void should_create_remote_cmr_hearing_notice_when_remote_hearing() {

        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(remoteCmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteCmrHearingNoticeDocumentCreator, times(1)).create(caseDetails);
        verify(cmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(adaCmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_ada_cmr_hearing_notice_when_accelerated_detained_appeal() {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(adaCmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(adaCmrHearingNoticeDocumentCreator, times(1)).create(caseDetails);
        verify(cmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(remoteCmrHearingNoticeDocumentCreator, never()).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_also_append_internal_cmr_listing_letter_when_detained_in_irc() {

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER);
    }

    @Test
    void should_also_append_internal_cmr_listing_letter_when_detained_in_prison() {

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.PRISON.getValue()));
        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER);
    }

    @Test
    void should_not_append_internal_cmr_listing_letter_when_detained_in_other_facility() {

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.OTHER.getValue()));
        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.CMR_LISTING);

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = cmrListingHearingNoticeCreator.canHandle(callbackStage, callback);

                if (event == Event.CMR_LISTING
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
    void should_have_earliest_dispatch_priority() {
        assertEquals(EARLIEST, cmrListingHearingNoticeCreator.getDispatchPriority());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
