package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailCaseUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@Component
public class BailSubmissionWithUtCreator implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentCreator<BailCase> bailSubmissionDocumentCreator;
    private final BailDocumentHandler bailDocumentHandler;

    public BailSubmissionWithUtCreator(
        @Qualifier("bailSubmissionWithUt") DocumentCreator<BailCase> bailSubmissionDocumentCreator,
        BailDocumentHandler bailDocumentHandler
    ) {
        this.bailSubmissionDocumentCreator = bailSubmissionDocumentCreator;
        this.bailDocumentHandler = bailDocumentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.SUBMIT_APPLICATION
                    || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                    || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)
                && BailCaseUtils.isImaEnabled(callback.getCaseDetails().getCaseData());
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

        Document bailSubmission = bailSubmissionDocumentCreator.create(caseDetails);

        bailDocumentHandler.addWithMetadata(
            bailCase,
            bailSubmission,
            APPLICANT_DOCUMENTS_WITH_METADATA,
            DocumentTag.BAIL_SUBMISSION
        );

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}

