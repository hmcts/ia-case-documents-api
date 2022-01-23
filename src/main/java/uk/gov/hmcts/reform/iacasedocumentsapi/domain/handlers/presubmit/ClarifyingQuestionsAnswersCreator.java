package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@Component
public class ClarifyingQuestionsAnswersCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> clarifyingQuestionsAnswersDocumentCreator;
    private final DocumentHandler documentHandler;

    public ClarifyingQuestionsAnswersCreator(
        @Qualifier("clarifyingQuestionsAnswers") DocumentCreator<AsylumCase> clarifyingQuestionsAnswersDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.clarifyingQuestionsAnswersDocumentCreator = clarifyingQuestionsAnswersDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        // TODO: do we need to support the CompleteClarifyingQuestions event as well?
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Objects.equals(Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS, callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Document clarifyingQuestionsAnswersDocument = clarifyingQuestionsAnswersDocumentCreator.create(caseDetails);
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            clarifyingQuestionsAnswersDocument,
            // ia-aip-frontend and case-api is still using this to show the appellant documents
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.ADDITIONAL_EVIDENCE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
