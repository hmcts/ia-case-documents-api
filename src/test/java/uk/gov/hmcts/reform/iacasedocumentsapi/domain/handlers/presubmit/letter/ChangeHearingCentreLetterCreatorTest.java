package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeHearingCentreLetterCreatorTest {
    @Mock
    private DocumentCreator<AsylumCase> internalDetainedEditCaseListingLetterCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;
    private ChangeHearingCentreLetterCreator changeHearingCentreLetterCreator;

    @BeforeEach
    public void setUp() {
        changeHearingCentreLetterCreator =
            new ChangeHearingCentreLetterCreator(
                internalDetainedEditCaseListingLetterCreator,
                documentHandler
            );
    }

    @Test
    public void should_create_internal_detained_edit_case_listing_pdf_and_append_to_notifications_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(callback.getEvent()).thenReturn(Event.CHANGE_HEARING_CENTRE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(internalDetainedEditCaseListingLetterCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase, uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER
        );
    }

    @Test
    public void should_create_internal_detained_ada_edit_case_listing_pdf_and_append_to_notifications_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(callback.getEvent()).thenReturn(Event.CHANGE_HEARING_CENTRE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(internalDetainedEditCaseListingLetterCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
                asylumCase, uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_non_ada_case_details_before_is_missing() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_HEARING_CENTRE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(internalDetainedEditCaseListingLetterCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("previous case data is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_ada_case_details_before_is_missing() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_HEARING_CENTRE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(internalDetainedEditCaseListingLetterCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("previous case data is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = changeHearingCentreLetterCreator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_is_detained_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = changeHearingCentreLetterCreator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentreLetterCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
