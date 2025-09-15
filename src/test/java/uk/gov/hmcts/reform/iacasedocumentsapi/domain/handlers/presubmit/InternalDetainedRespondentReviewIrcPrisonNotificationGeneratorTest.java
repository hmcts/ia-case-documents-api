
package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter.InternalDetainedRespondentReviewIrcPrisonNotificationGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;


import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetainedRespondentReviewIrcPrisonNotificationGeneratorTest {


    @Mock private DocumentCreator<AsylumCase> documentCreator;
    @Mock private DocumentHandler documentHandler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document document;

    private InternalDetainedRespondentReviewIrcPrisonNotificationGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new InternalDetainedRespondentReviewIrcPrisonNotificationGenerator(documentCreator, documentHandler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);

    }

    @Test
    void canHandle_returnsTrue_whenAllConditionsMet() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn((Optional.of("prison")),Optional.of("irc"));

        boolean result = generator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(result).isTrue();
    }

    @Test
    void canHandle_returnsFalse_ifStageNotAboutToSubmit() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        boolean result = generator.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(result).isFalse();
    }

    @Test
    void handle_createsDocument_andAddsWithMetadata() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn((Optional.of("prison")),Optional.of("irc"));
        when(documentCreator.create(caseDetails)).thenReturn(document);

        PreSubmitCallbackResponse<AsylumCase> response = generator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isEqualTo(asylumCase);

        verify(documentHandler).addWithMetadata(
                asylumCase,
                document,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_PRISON_IRC_RESPONDER_REVIEW_NOTICE_LETTER
        );
    }

    @Test
    void handle_throwsException_ifCanHandleFalse() {
        assertThatThrownBy(() -> generator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }
}
