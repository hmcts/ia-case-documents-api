package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;

@Component
public class HoReviewEvidenceLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hoReviewEvidenceLetterCreator;
    private final DocumentHandler documentHandler;

    public HoReviewEvidenceLetterGenerator(
            @Qualifier("hoReviewEvidenceLetter") DocumentCreator<AsylumCase> hoReviewEvidenceLetterCreator,
            DocumentHandler documentHandler
    ) {
        this.hoReviewEvidenceLetterCreator = hoReviewEvidenceLetterCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && AsylumCaseUtils.isInternalCase(callback.getCaseDetails().getCaseData())
                && AsylumCaseUtils.isAppellantInDetention(callback.getCaseDetails().getCaseData());
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

        Document hoReviewEvidenceLetter = hoReviewEvidenceLetterCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                hoReviewEvidenceLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.REQUEST_RESPONDENT_REVIEW
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
