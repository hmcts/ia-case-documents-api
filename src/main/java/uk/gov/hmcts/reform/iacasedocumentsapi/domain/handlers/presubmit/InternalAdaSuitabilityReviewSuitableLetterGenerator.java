package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
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
public class InternalAdaSuitabilityReviewSuitableLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalAdaSuitabilityReviewSuitableLetterGenerator;
    private final DocumentHandler documentHandler;

    public InternalAdaSuitabilityReviewSuitableLetterGenerator(
        @Qualifier("internalAdaSuitabilitySuitable") DocumentCreator<AsylumCase> internalAdaSuitabilityReviewSuitableLetterGenerator,
        DocumentHandler documentHandler
    ) {
        this.internalAdaSuitabilityReviewSuitableLetterGenerator = internalAdaSuitabilityReviewSuitableLetterGenerator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callback.getEvent() == Event.ADA_SUITABILITY_REVIEW
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && isInternalCase(asylumCase)
               && isAcceleratedDetainedAppeal(asylumCase);
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

        Document internalAdaSuitabilitySuitableLetterDocument = internalAdaSuitabilityReviewSuitableLetterGenerator.create(caseDetails);

        final AdaSuitabilityReviewDecision suitabilityDecision =
            asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability decision is missing."));

        if (suitabilityDecision.equals(AdaSuitabilityReviewDecision.SUITABLE)) {
            documentHandler.addWithMetadata(
                asylumCase,
                internalAdaSuitabilitySuitableLetterDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_ADA_SUITABILITY
            );
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
