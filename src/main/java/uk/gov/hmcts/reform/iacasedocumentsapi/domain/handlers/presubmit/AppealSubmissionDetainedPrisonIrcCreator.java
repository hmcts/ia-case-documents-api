package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
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

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Component
public class AppealSubmissionDetainedPrisonIrcCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    private final DocumentHandler documentHandler;

    public AppealSubmissionDetainedPrisonIrcCreator(
        @Qualifier("appealSubmission") DocumentCreator<AsylumCase> appealSubmissionDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.appealSubmissionDocumentCreator = appealSubmissionDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        RemissionType remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
        boolean isRemissionPresent = Arrays.asList(
                HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPEAL
                && (isInternalCase(asylumCase) && hasBeenSubmittedByAppellantInternalCase(asylumCase))
                && isDetainedInOneOfFacilityTypes(asylumCase, PRISON, IRC)
                && isRemissionPresent
                && !isSubmissionOutOfTime(asylumCase);
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

        Document appealSubmission = appealSubmissionDocumentCreator.create(caseDetails);

        documentHandler.addWithMetadata(
            asylumCase,
            appealSubmission,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
