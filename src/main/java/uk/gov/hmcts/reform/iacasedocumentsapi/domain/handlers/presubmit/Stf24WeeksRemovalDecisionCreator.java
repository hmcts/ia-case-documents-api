package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOVAL_OF_24W_APPLICATION_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Slf4j
@Component
public class Stf24WeeksRemovalDecisionCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> stf24WeeksRemovalDecisionDocumentCreator;
    private final DocumentHandler documentHandler;

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public Stf24WeeksRemovalDecisionCreator(
        @Qualifier("stf24WeeksRemovalDecisionDocumentCreator") DocumentCreator<AsylumCase> stf24WeeksRemovalDecisionDocumentCreator,
        DocumentHandler documentHandler) {
        this.stf24WeeksRemovalDecisionDocumentCreator = stf24WeeksRemovalDecisionDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean applicationRefused = callback.getEvent() == Event.DECIDE_AN_APPLICATION
            && asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class).orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);
        boolean stfRemoved = callback.getEvent() == Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
        return applicationRefused || stfRemoved;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();
        DocumentTag tag = callback.getEvent() == Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS
            ? DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
            : DocumentTag.STF_24WEEKS_REMOVAL_REFUSED_DECISION_DOCUMENT;
        Document appealSubmission = stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails);
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            appealSubmission,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            tag
        );

        if (isInternalCase(asylumCase)) {
            documentHandler.addWithMetadata(
                asylumCase,
                appealSubmission,
                LETTER_NOTIFICATION_DOCUMENTS,
                tag
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

