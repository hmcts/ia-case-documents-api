package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

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
public class InternalRequestBuildCaseDocumentGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalAdaBuildCaseDocumentGenerator;
    private final DocumentCreator<AsylumCase> internalDetainedBuildCaseDocumentGenerator;
    private final DocumentHandler documentHandler;

    public InternalRequestBuildCaseDocumentGenerator(
            @Qualifier("internalAdaRequestBuildCase") DocumentCreator<AsylumCase> internalAdaRequestBuildCaseDocumentCreator,
            @Qualifier("internalDetainedRequestBuildCase") DocumentCreator<AsylumCase> internalDetainedRequestBuildCaseDocumentCreator,
            DocumentHandler documentHandler
    ) {
        this.internalAdaBuildCaseDocumentGenerator = internalAdaRequestBuildCaseDocumentCreator;
        this.internalDetainedBuildCaseDocumentGenerator = internalDetainedRequestBuildCaseDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REQUEST_CASE_BUILDING
            && isInternalCase(asylumCase)
            && isDetainedAppeal(asylumCase);
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

        Document internalBuildCaseDocument = isAcceleratedDetainedAppeal(asylumCase)
            ?
                internalAdaBuildCaseDocumentGenerator.create(caseDetails) : internalDetainedBuildCaseDocumentGenerator.create(caseDetails);

        documentHandler.addWithMetadata(
                asylumCase,
                internalBuildCaseDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.REQUEST_CASE_BUILDING
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}


