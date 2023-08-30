package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

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
public class InternalApplyForFtpaAppellantLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> internalApplyForFtpaAppellantLetterCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;
    private InternalApplyForFtpaAppellantLetterGenerator internalApplyForFtpaAppellantLetterGenerator;

    @BeforeEach
    public void setUp() {
        internalApplyForFtpaAppellantLetterGenerator =
                new InternalApplyForFtpaAppellantLetterGenerator(
                        internalApplyForFtpaAppellantLetterCreator,
                        documentHandler
                );

        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(internalApplyForFtpaAppellantLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);
    }

    @Test
    public void should_create_internal_ftpa_submitted_appellant_pdf_and_append_to_notification_attachment_documents_for_the_case() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalApplyForFtpaAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalApplyForFtpaAppellantLetterGenerator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalApplyForFtpaAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalApplyForFtpaAppellantLetterGenerator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalApplyForFtpaAppellantLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void it_should_only_handle_about_to_submit_and_apply_for_ftpa_appellant_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalApplyForFtpaAppellantLetterGenerator.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent().equals(Event.APPLY_FOR_FTPA_APPELLANT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_appellant_is_not_in_detention() {
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = internalApplyForFtpaAppellantLetterGenerator.canHandle(callbackStage, callback);
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    public void it_should_only_handle_internal_cases(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalApplyForFtpaAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo.equals(YesOrNo.YES)) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    public void it_should_handle_both_detained_non_ada_and_ada_cases(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalApplyForFtpaAppellantLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

}
