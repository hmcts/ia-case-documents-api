package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCaseListedLegalRepLetterBundlerTest {

    private InternalCaseListedLegalRepLetterBundler internalCaseListedLetterHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock
    private DocumentBundler documentBundler;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Document bundleDocument;
    private String fileExtension = "PDF";
    private String fileName = "some-file-name";

    @BeforeEach
    public void setUp() {

        internalCaseListedLetterHandler =
            new InternalCaseListedLegalRepLetterBundler(
                fileExtension,
                fileName,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    public void it_can_handle_callback(InternalCaseListedLegalRepLetterBundlerTest.TestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        boolean canHandle = internalCaseListedLetterHandler.canHandle(scenario.callbackStage, callback);

        assertEquals(canHandle, scenario.isExpected());
    }

    private static List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> generateDifferentEventScenarios() {
        return InternalCaseListedLegalRepLetterBundlerTest.TestScenario.builder();
    }

    @Value
    static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expected;

        public static List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> builder() {
            List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> testScenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
            }
            return testScenarios;
        }
    }

    @Test
    public void it_should_not_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        internalCaseListedLetterHandler =
            new InternalCaseListedLegalRepLetterBundler(
                fileExtension,
                fileName,
                false,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        boolean canHandle = internalCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_when_canHandle_returns_false() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(internalCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertThatThrownBy(() -> internalCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void set_to_late_dispatch() {
        assertThat(internalCaseListedLetterHandler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertThatThrownBy(() -> internalCaseListedLetterHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> internalCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
            new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata() {

        return
            new DocumentWithMetadata(createDocumentWithDescription(),
                RandomStringUtils.randomAlphabetic(20),
                new SystemDateProvider().now().toString(), DocumentTag.INTERNAL_CASE_LISTED_LETTER,"test");

    }
}
