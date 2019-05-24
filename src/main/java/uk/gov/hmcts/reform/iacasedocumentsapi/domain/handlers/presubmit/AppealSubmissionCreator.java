package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
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
public class AppealSubmissionCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public AppealSubmissionCreator(
        @Qualifier("appealSubmission") DocumentCreator<AsylumCase> appealSubmissionDocumentCreator,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.appealSubmissionDocumentCreator = appealSubmissionDocumentCreator;
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
               && callback.getEvent() == Event.SUBMIT_APPEAL;
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

        Document appealSubmission = appealSubmissionDocumentCreator.create(caseDetails);

        attachDocumentToCase(asylumCase, appealSubmission);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document appealSubmission
    ) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments = asylumCase
                .read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            maybeDocuments
                .orElse(Collections.emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                appealSubmission,
                "",
                DocumentTag.APPEAL_SUBMISSION
            );

        List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments =
            documentsAppender.append(
                legalRepresentativeDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.APPEAL_SUBMISSION
            );

        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
    }
}
