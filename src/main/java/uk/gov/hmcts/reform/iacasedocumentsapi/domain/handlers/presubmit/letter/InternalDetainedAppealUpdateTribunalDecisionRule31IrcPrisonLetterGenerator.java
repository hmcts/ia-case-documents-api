package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasAppealBeenSubmittedByAppellantInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

@Component
public class InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterGenerator(
            @Qualifier("internalDetainedUpdateTribunalDecisionRule31IrcPrisonLetterDocumentCreator") DocumentCreator<AsylumCase> documentCreator,
            DocumentHandler documentHandler
    ) {
        this.documentCreator = documentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();



        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                && isInternalCase(asylumCase)
                && hasAppealBeenSubmittedByAppellantInternalCase(asylumCase)
                && isDetainedInOneOfFacilityTypes(asylumCase, PRISON, IRC)
                && isRule31ReasonUpdatingDecision(asylumCase);
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

        Document document = documentCreator.create(caseDetails);

        documentHandler.addWithMetadata(
                asylumCase,
                document,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_APPEAL_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isRule31ReasonUpdatingDecision(AsylumCase asylumCase) {
        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)
                .map(type -> type.equals(UNDER_RULE_31)).orElse(false);
    }
}
