package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

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
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Component
public class InternalDetainedAdjournHearingWithoutDateLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> adjournHearingWithoutDateDocumentCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedAdjournHearingWithoutDateLetterGenerator(
        @Qualifier("internalAdjournHearingWithoutDateLetter") DocumentCreator<AsylumCase> adjournHearingWithoutDateDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.adjournHearingWithoutDateDocumentCreator = adjournHearingWithoutDateDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && isInternalCase(asylumCase)
               && isAppellantInDetention(asylumCase)
               && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore()
                .orElseThrow(() -> new IllegalStateException("previous case data is not present"));
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Document letter = adjournHearingWithoutDateDocumentCreator.create(caseDetails, caseDetailsBefore);

        documentHandler.addWithMetadata(
                asylumCase,
                letter,
                LEGAL_REPRESENTATIVE_DOCUMENTS,
                DocumentTag.INTERNAL_ADJOURN_HEARING_WITHOUT_DATE_LETTER
        );

       documentHandler.addWithMetadata(
            asylumCase,
            letter,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_ADJOURN_HEARING_WITHOUT_DATE_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}

