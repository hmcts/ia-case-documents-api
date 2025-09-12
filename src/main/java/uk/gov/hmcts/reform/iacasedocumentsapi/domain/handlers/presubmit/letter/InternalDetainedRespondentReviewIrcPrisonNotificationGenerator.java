package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;


public class InternalDetainedRespondentReviewIrcPrisonNotificationGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalDetainedRespondentReviewIrcPrisonNotificationCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedRespondentReviewIrcPrisonNotificationGenerator(
            @Qualifier("internalDetainedRespondentReviewIrcPrisonNotificationLetter") DocumentCreator<AsylumCase> internalDetainedRespondetReviewIrcPrisonNotificationCreator,
            DocumentHandler documentHandler
    ) {
        this.internalDetainedRespondentReviewIrcPrisonNotificationCreator = internalDetainedRespondetReviewIrcPrisonNotificationCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
        && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
        && (isDetainedInOneOfFacilityTypes(callback.getCaseDetails().getCaseData(),PRISON, IRC))
        && (isInternalCase(callback.getCaseDetails().getCaseData()) && !hasBeenSubmittedAsLegalRepresentedInternalCase(callback.getCaseDetails().getCaseData()));
        }

        // Introduce hasBeenSubmittedAsLegalRepresentedInternalCase as is in notifications API
    public static boolean hasBeenSubmittedAsLegalRepresentedInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
                .map(yesOrNo -> Objects.equals(NO, yesOrNo)).orElse(false);
    } // put in utyls file

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Document hoReviewEvidenceLetter = internalDetainedRespondentReviewIrcPrisonNotificationCreator.create(caseDetails);
        documentHandler.addWithMetadata(
                asylumCase,
                hoReviewEvidenceLetter,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_PRISON_IRC_RESPONDER_REVIEW_NOTICE_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
