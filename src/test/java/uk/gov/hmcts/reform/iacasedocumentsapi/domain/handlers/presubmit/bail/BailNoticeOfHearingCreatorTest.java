package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent.RELISTING;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
class BailNoticeOfHearingCreatorTest {

    @Mock private DocumentCreator<BailCase> bailInitialListingNoticeOfHearingCreator;
    @Mock private DocumentCreator<BailCase> bailRelistingNoticeOfHearingCreator;
    @Mock private DocumentCreator<BailCase> bailConditionalBailRelistingNoticeOfHearingCreator;
    @Mock private BailDocumentHandler bailDocumentHandler;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document bailNoticeOfHearingDocument;

    private BailNoticeOfHearingCreator bailNoticeOfHearingCreator;

    @BeforeEach
    void setUp() {
        bailNoticeOfHearingCreator = new BailNoticeOfHearingCreator(
            bailInitialListingNoticeOfHearingCreator,
            bailRelistingNoticeOfHearingCreator,
            bailConditionalBailRelistingNoticeOfHearingCreator,
            bailDocumentHandler);
    }

    @Test
    void should_handle_valid_events_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = bailNoticeOfHearingCreator.canHandle(preSubmitCallbackStage, callback);
                if (event == Event.CASE_LISTING && preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_create_notice_of_hearing_for_initial_listing_event_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));
        when(bailInitialListingNoticeOfHearingCreator.create(caseDetails)).thenReturn(bailNoticeOfHearingDocument);

        PreSubmitCallbackResponse<BailCase> response = bailNoticeOfHearingCreator
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .appendWithMetadata(bailCase, bailNoticeOfHearingDocument,
                BailCaseFieldDefinition.HEARING_DOCUMENTS, DocumentTag.BAIL_NOTICE_OF_HEARING);
    }

    @Test
    void should_create_notice_of_hearing_for_relisting_event_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(RELISTING));
        when(bailRelistingNoticeOfHearingCreator.create(caseDetails)).thenReturn(bailNoticeOfHearingDocument);

        PreSubmitCallbackResponse<BailCase> response = bailNoticeOfHearingCreator
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .appendWithMetadata(bailCase, bailNoticeOfHearingDocument,
                BailCaseFieldDefinition.HEARING_DOCUMENTS, DocumentTag.BAIL_NOTICE_OF_HEARING);
    }

    @Test
    void should_create_notice_of_hearing_for_conditional_bail_relisting_event_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(RELISTING));
        when(bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, String.class))
            .thenReturn(Optional.of(State.DECISION_CONDITIONAL_BAIL.toString()));
        when(bailConditionalBailRelistingNoticeOfHearingCreator.create(caseDetails))
            .thenReturn(bailNoticeOfHearingDocument);

        PreSubmitCallbackResponse<BailCase> response = bailNoticeOfHearingCreator
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .appendWithMetadata(bailCase, bailNoticeOfHearingDocument,
                BailCaseFieldDefinition.HEARING_DOCUMENTS, DocumentTag.BAIL_NOTICE_OF_HEARING);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailNoticeOfHearingCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailNoticeOfHearingCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailNoticeOfHearingCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailNoticeOfHearingCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy((() -> bailNoticeOfHearingCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

}
