package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;

@Component
public class DecisionAndReasonsCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> decisionAndReasonsDocumentCreator;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public DecisionAndReasonsCreator(
        @Qualifier("decisionAndReasons") DocumentCreator<AsylumCase> decisionAndReasonsDocumentCreator,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.decisionAndReasonsDocumentCreator = decisionAndReasonsDocumentCreator;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.GENERATE_DECISION_AND_REASONS;
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

        Document decisionAndReasons = decisionAndReasonsDocumentCreator.create(caseDetails);

        attachDocumentToCase(asylumCase, decisionAndReasons);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document decisionAndReasons
    ) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments = asylumCase
                .read(DRAFT_DECISION_AND_REASONS_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> decisionAndReasonsDocuments =
            maybeDocuments
                .orElse(emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                decisionAndReasons,
                "",
                DocumentTag.DECISION_AND_REASONS_DRAFT
            );

        List<IdValue<DocumentWithMetadata>> allDecisionAndReasonsDocuments =
            documentsAppender.append(
                decisionAndReasonsDocuments,
                singletonList(documentWithMetadata),
                DocumentTag.DECISION_AND_REASONS_DRAFT
            );

        asylumCase.write(DRAFT_DECISION_AND_REASONS_DOCUMENTS, allDecisionAndReasonsDocuments);
    }
}
