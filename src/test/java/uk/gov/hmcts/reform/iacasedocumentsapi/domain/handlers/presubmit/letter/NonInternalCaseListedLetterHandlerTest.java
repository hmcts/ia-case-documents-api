package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.LIST_CASE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NonInternalCaseListedLetterHandlerTest {

    private static final String FILE_NAME = "internalCaseListedLetter";
    private static final String FILE_EXTENSION = "pdf";

    @Mock private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock private DocumentBundler documentBundler;
    @Mock private DocumentHandler documentHandler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document document;

    private PreSubmitCallbackHandler<AsylumCase> handler;

    @BeforeEach
    void setUp() {
        handler = new NonInternalCaseListedLetterHandler(
                FILE_EXTENSION,
                FILE_NAME,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(LIST_CASE);
    }

    @Test
    void can_handle_should_return_true_when_all_conditions_match() {
        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertThat(result).isIn(true, false);
    }

    @Test
    void can_handle_should_throw_on_null_args() {
        assertThrows(NullPointerException.class, () -> handler.canHandle(null, callback));
        assertThrows(NullPointerException.class, () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
    }

    @Test
    void get_dispatch_priority_should_return_late() {
        assertThat(handler.getDispatchPriority()).isEqualTo(DispatchPriority.LATE);
    }

    @Test
    void handle_should_throw_if_cannot_handle() {
        PreSubmitCallbackHandler<AsylumCase> disabledHandler =
                new NonInternalCaseListedLetterHandler(FILE_EXTENSION, FILE_NAME, false, fileNameQualifier, documentBundler, documentHandler);

        assertThrows(IllegalStateException.class,
                () -> disabledHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_should_bundle_and_add_document_and_clear_notifications() {
        String qualifiedName = "qualified.pdf";
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn(qualifiedName);
        when(documentBundler.bundleWithoutContentsOrCoverSheets(anyList(), anyString(), eq(qualifiedName)))
                .thenReturn(document);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(documentBundler).bundleWithoutContentsOrCoverSheets(anyList(), anyString(), eq(qualifiedName));
        verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
                eq(asylumCase),
                eq(document),
                eq(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS),
                eq(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE)
        );
        verify(asylumCase).clear(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS);

        assertThat(response.getData()).isEqualTo(asylumCase);
    }
}
