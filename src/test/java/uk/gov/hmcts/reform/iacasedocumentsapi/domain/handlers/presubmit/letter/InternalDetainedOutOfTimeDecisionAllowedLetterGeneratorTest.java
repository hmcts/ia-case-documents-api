package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedOutOfTimeDecisionAllowedLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> documentCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private DocumentBundler documentBundler;
    @Mock
    private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;
    @Mock
    private Document bundledDocument;

    private InternalDetainedOutOfTimeDecisionAllowedLetterGenerator letterGenerator;

    @BeforeEach
    void setUp() {
        letterGenerator = new InternalDetainedOutOfTimeDecisionAllowedLetterGenerator(
                "pdf",
                "test-file",
                documentCreator,
                documentHandler,
                documentBundler,
                fileNameQualifier
        );
    }

    @Test
    void should_create_letter_bundle_and_append_to_notification_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();
        when(documentCreator.create(caseDetails)).thenReturn(uploadedDocument);
        when(fileNameQualifier.get("test-file.pdf", caseDetails)).thenReturn("qualified-filename.pdf");
        when(documentBundler.bundleWithoutContentsOrCoverSheets(anyList(), eq("Letter bundle documents"), eq("qualified-filename.pdf")))
                .thenReturn(bundledDocument);

        // Mock decision of notice documents
        List<IdValue<DocumentWithMetadata>> decisionOfNoticeDocuments = new ArrayList<>();
        IdValue<DocumentWithMetadata> decisionDoc = new IdValue<>(
                "1",
                new DocumentWithMetadata(
                        uploadedDocument,
                        "Decision notice",
                        "2018-12-25",
                        DocumentTag.DECISION_AND_REASONS_DRAFT,
                        "The respondent",
                        "TCW"
                )
        );
        decisionOfNoticeDocuments.add(decisionDoc);
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.of(decisionOfNoticeDocuments));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                bundledDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER
        );
    }

    @Test
    void should_create_letter_bundle_without_decision_documents_when_none_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();
        when(documentCreator.create(caseDetails)).thenReturn(uploadedDocument);
        when(fileNameQualifier.get("test-file.pdf", caseDetails)).thenReturn("qualified-filename.pdf");
        when(documentBundler.bundleWithoutContentsOrCoverSheets(anyList(), eq("Letter bundle documents"), eq("qualified-filename.pdf")))
                .thenReturn(bundledDocument);

        // Mock empty decision of notice documents
        when(asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                bundledDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER
        );
    }

    @Test
    void should_handle_callback_when_all_conditions_met() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_if_not_record_out_of_time_decision_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_if_not_about_to_submit_stage() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCaseScenarios")
    void should_not_handle_callback_for_invalid_conditions(String scenarioName, Consumer<AsylumCase> invalidConditionSetup) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCaseExcept();
        invalidConditionSetup.accept(asylumCase);

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    private static Stream<Arguments> provideInvalidCaseScenarios() {
        return Stream.of(
                Arguments.of("not internal case",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO))
                ),
                Arguments.of("not detained",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO))
                ),
                Arguments.of("appellant in detention is missing",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty())
                ),
                Arguments.of("is admin is missing",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty())
                ),
                Arguments.of("in other detention facility",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"))
                ),
                Arguments.of("detention facility is missing",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty())
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"immigrationRemovalCentre", "prison"})
    void should_handle_callback_for_valid_detention_facilities(String detentionFacility) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCaseExcept();
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacility));

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_throw_exception_when_cannot_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> letterGenerator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_for_all_events_except_record_out_of_time_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            setUpValidCase();

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = letterGenerator.canHandle(callbackStage, callback);

                if (event == Event.RECORD_OUT_OF_TIME_DECISION && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_handle_callback_when_internal_case_and_detained_in_irc() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_handle_callback_when_internal_case_and_detained_in_prison() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_when_not_internal_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_when_appellant_not_detained() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_when_in_invalid_detention_facility() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_OUT_OF_TIME_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    private void setUpValidCase() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
    }

    private void setUpValidCaseExcept() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
    }
}
