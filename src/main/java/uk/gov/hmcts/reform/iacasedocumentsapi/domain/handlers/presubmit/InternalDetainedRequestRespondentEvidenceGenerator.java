package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

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
public class InternalDetainedRequestRespondentEvidenceGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDetainedRequestRespondentEvidenceCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedRequestRespondentEvidenceGenerator(
            @Qualifier("internalDetainedRequestRespondentEvidence") DocumentCreator<AsylumCase> internalDetainedRequestRespondentEvidenceCreator,
            DocumentHandler documentHandler
    ) {
        this.internalDetainedRequestRespondentEvidenceCreator = internalDetainedRequestRespondentEvidenceCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && AsylumCaseUtils.isInternalCase(asylumCase)
                && AsylumCaseUtils.isAppellantInDetention(asylumCase)
                && !isAcceleratedDetainedAppeal(asylumCase);
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

        Document internalDetainedRequestRespondentEvidenceLetter = internalDetainedRequestRespondentEvidenceCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                internalDetainedRequestRespondentEvidenceLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
