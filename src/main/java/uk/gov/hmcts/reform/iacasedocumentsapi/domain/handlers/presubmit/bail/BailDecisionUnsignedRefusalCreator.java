package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.BailCaseUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@Component
public class BailDecisionUnsignedRefusalCreator implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentCreator<BailCase> bailDocumentCreator;
    private final BailDocumentHandler bailDocumentHandler;

    public BailDecisionUnsignedRefusalCreator(
        @Qualifier("decisionUnsignedRefusal") DocumentCreator<BailCase> bailDocumentCreator,
        BailDocumentHandler bailDocumentHandler
    ) {
        this.bailDocumentCreator = bailDocumentCreator;
        this.bailDocumentHandler = bailDocumentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        final BailCase bailCase = caseDetails.getCaseData();

        boolean isRefused = bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)
            .map(type -> type.equals(RecordDecisionType.REFUSED))
            .orElse(false);

        boolean isRefusedUnderIma = bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)
            .map(type -> type.equals(RecordDecisionType.REFUSED_UNDER_IMA))
            .orElse(false);

        boolean isMindedToGrant = (BailCaseUtils.isImaEnabled(bailCase)
            ? bailCase.read(RECORD_THE_DECISION_LIST_IMA, String.class).orElse("")
            : bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse(""))
                .equalsIgnoreCase("mindedToGrant");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.RECORD_THE_DECISION && (isRefused || isRefusedUnderIma) && !isMindedToGrant;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        final BailCase bailCase = caseDetails.getCaseData();

        Document bailDocument = bailDocumentCreator.create(caseDetails);

        bailDocumentHandler.addWithMetadata(
            bailCase,
            bailDocument,
            UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA,
            DocumentTag.BAIL_DECISION_UNSIGNED
        );

        bailDocumentHandler.addDocumentWithoutMetadata(
            bailCase,
            bailDocument,
            DECISION_UNSIGNED_DOCUMENT
        );

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}

