package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

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

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@Component
public class ADASuitabilityDocumentGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> adaSuitabilityDocumentGenerator;
    private final DocumentHandler documentHandler;

    public ADASuitabilityDocumentGenerator(
            @Qualifier("adaSuitability") DocumentCreator<AsylumCase> appealSubmissionDocumentCreator,
            DocumentHandler documentHandler
    ) {
        this.adaSuitabilityDocumentGenerator = appealSubmissionDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.ADA_SUITABILITY_REVIEW &&
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
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

        Document adaSuitabilityDocument = adaSuitabilityDocumentGenerator.create(caseDetails);

        documentHandler.addWithMetadata(
                asylumCase,
                adaSuitabilityDocument,
                TRIBUNAL_DOCUMENTS,
                DocumentTag.ADA_SUITABILITY
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
