package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_DOCUMENTS;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingNoticeEditedCreatorTest {

    @Mock private DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator;
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

    @Before
    public void setUp() {

        hearingNoticeEditedCreator =
            new HearingNoticeEditedCreator(
                hearingNoticeUpdatedRequirementsDocumentCreator,
                hearingNoticeUpdatedDetailsDocumentCreator,
                documentHandler,
                hearingDetailsFinder
            );

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);
        when(hearingNoticeUpdatedDetailsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);
    }

    @Test
    public void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {

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

        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    public void should_create_hearing_notice_pdf_for_updated_hearing_date_and_append_to_legal_representative_documents_for_the_case() {

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

        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    public void should_create_hearing_notice_pdf_for_updated_hearing_centre_and_append_to_legal_representative_documents_for_the_case() {

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

        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    public void should_throw_when_no_previous_case_data_exists() {

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("previous case data is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

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
