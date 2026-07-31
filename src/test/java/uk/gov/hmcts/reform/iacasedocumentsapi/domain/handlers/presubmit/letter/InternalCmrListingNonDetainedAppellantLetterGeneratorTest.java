package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.EARLY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
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
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCmrListingNonDetainedAppellantLetterGeneratorTest {

    @Mock private DocumentCreator<AsylumCase> documentCreator;
    @Mock private DocumentHandler documentHandler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;

    private InternalCmrListingNonDetainedAppellantLetterGenerator internalCmrListingNonDetainedAppellantLetterGenerator;

    @BeforeEach
    public void setUp() {
        internalCmrListingNonDetainedAppellantLetterGenerator =
            new InternalCmrListingNonDetainedAppellantLetterGenerator(documentCreator, documentHandler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
    }

    @Test
    public void should_create_internal_cmr_listing_appellant_letter_and_append_to_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(documentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalCmrListingNonDetainedAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentCreator, times(1)).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument, LETTER_NOTIFICATION_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_APPELLANT_LETTER);
    }

    @Test
    public void it_can_handle_callback() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertTrue(internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CMR_LISTING"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_non_cmr_listing_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_wrong_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertFalse(internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(stage, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_not_internal_case() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty());

        assertFalse(internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_appellant_is_not_legally_represented() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void should_have_early_dispatch_priority() {
        assertThat(internalCmrListingNonDetainedAppellantLetterGenerator.getDispatchPriority()).isEqualTo(EARLY);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCmrListingNonDetainedAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
