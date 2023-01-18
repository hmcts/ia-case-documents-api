package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingNoticeEditedCreatorTest {

    @Mock private DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> adaHearingNoticeUpdatedDetailsDocumentCreator;
    @Mock private HearingDetailsFinder hearingDetailsFinder;
    @Mock private DocumentHandler documentHandler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;

    private HearingNoticeEditedCreator hearingNoticeEditedCreator;

    private String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private String oldHearingDate = "2020-01-05T12:30:00";

    @BeforeEach
    public void setUp() {

        hearingNoticeEditedCreator =
            new HearingNoticeEditedCreator(
                hearingNoticeUpdatedRequirementsDocumentCreator,
                hearingNoticeUpdatedDetailsDocumentCreator,
                remoteHearingNoticeUpdatedDetailsDocumentCreator,
                adaHearingNoticeUpdatedDetailsDocumentCreator,
                documentHandler,
                hearingDetailsFinder
            );
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_feature_flag_enabled() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_for_remote_hearing() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_for_remote_hearing_reheard_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_reheard_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_for_updated_hearing_date_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String listCaseHearingCentre = hearingCentreNameBefore;
        final String hearingDate = "2020-02-05T12:30:00";

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        //verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_for_updated_hearing_centre_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String listCaseHearingCentre = HearingCentre.GLASGOW.toString();
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        //verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_create_hearing_notice_pdf_for_ada_updated_hearing_centre_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String listCaseHearingCentre = HearingCentre.GLASGOW.toString();
        final String hearingDate = oldHearingDate;

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.getCaseData())).thenReturn(oldHearingDate);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(adaHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        //verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void should_throw_when_no_previous_case_data_exists() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("previous case data is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingNoticeEditedCreator.canHandle(callbackStage, callback);

                if ((event == Event.EDIT_CASE_LISTING)
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> hearingNoticeEditedCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
