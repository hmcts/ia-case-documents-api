package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
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
public class InternalFtpaDecidedLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalAppellantFtpaDecidedGrantedLetter;
    private final DocumentCreator<AsylumCase> internalHoFtpaDecidedGrantedLetter;
    private final DocumentCreator<AsylumCase> internalHoFtpaDecidedPartiallyGrantedLetter;
    private final DocumentCreator<AsylumCase> internalHoFtpaDecidedRefusedLetter;
    private final DocumentHandler documentHandler;
    private final String ftpaApplicantAppellant = "appellant";

    public InternalFtpaDecidedLetterGenerator(
            @Qualifier("internalAppellantFtpaDecidedGrantedLetter") DocumentCreator<AsylumCase> internalAppellantFtpaDecidedGrantedLetter,
            @Qualifier("internalHoFtpaDecidedGrantedLetter") DocumentCreator<AsylumCase> internalHoFtpaDecidedGrantedLetter,
            @Qualifier("internalHoFtpaDecidedPartiallyGrantedLetter") DocumentCreator<AsylumCase> internalHoFtpaDecidedPartiallyGrantedLetter,
            @Qualifier("internalHoFtpaDecidedRefusedLetter") DocumentCreator<AsylumCase> internalHoFtpaDecidedRefusedLetter,
            DocumentHandler documentHandler
    ) {
        this.internalAppellantFtpaDecidedGrantedLetter = internalAppellantFtpaDecidedGrantedLetter;
        this.internalHoFtpaDecidedGrantedLetter = internalHoFtpaDecidedGrantedLetter;
        this.internalHoFtpaDecidedPartiallyGrantedLetter = internalHoFtpaDecidedPartiallyGrantedLetter;
        this.internalHoFtpaDecidedRefusedLetter = internalHoFtpaDecidedRefusedLetter;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                && isInternalCase(asylumCase)
                && isAppellantInDetention(asylumCase);
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

        Optional<FtpaDecisionOutcomeType> ftpaAppellantDecisionOutcomeType = asylumCase
                .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        Optional<FtpaDecisionOutcomeType> ftpaRespondentDecisionOutcomeType = asylumCase
                .read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        Optional<String> ftpaApplicantType = asylumCase.read(FTPA_APPLICANT_TYPE, String.class);

        Document documentForUpload;
        DocumentTag documentTag;

        if (ftpaApplicantType.equals(Optional.of(ftpaApplicantAppellant))) {
            if (ftpaAppellantDecisionOutcomeType.equals(Optional.of(FTPA_GRANTED))) {
                documentForUpload = internalAppellantFtpaDecidedGrantedLetter.create(caseDetails);
            } else {
                return new PreSubmitCallbackResponse<>(asylumCase);
            }
            documentTag = DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER;
        } else {
            if (ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_GRANTED))) {
                documentForUpload = internalHoFtpaDecidedGrantedLetter.create(caseDetails);
            } else if (ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_PARTIALLY_GRANTED))) {
                documentForUpload = internalHoFtpaDecidedPartiallyGrantedLetter.create(caseDetails);
            } else if (ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_REFUSED))
                    || ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_NOT_ADMITTED))) {
                documentForUpload = internalHoFtpaDecidedRefusedLetter.create(caseDetails);
            } else {
                return new PreSubmitCallbackResponse<>(asylumCase);
            }
            documentTag = DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER;
        }

        documentHandler.addWithMetadata(
                asylumCase,
                documentForUpload,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                documentTag
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
