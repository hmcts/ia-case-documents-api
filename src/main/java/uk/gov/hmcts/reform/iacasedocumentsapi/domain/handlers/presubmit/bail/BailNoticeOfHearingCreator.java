package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent.INITIAL_LISTING;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@Component
public class BailNoticeOfHearingCreator implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentCreator<BailCase> bailInitialListingNoticeOfHearingCreator;
    private final DocumentCreator<BailCase> bailRelistingNoticeOfHearingCreator;
    private final DocumentCreator<BailCase> bailConditionalBailRelistingNoticeOfHearingCreator;
    private final BailDocumentHandler bailDocumentHandler;

    public BailNoticeOfHearingCreator(
        @Qualifier("bailNoticeOfHearingInitialListing") DocumentCreator<BailCase> bailInitialListingNoticeOfHearingCreator,
        @Qualifier("bailNoticeOfHearingRelisting") DocumentCreator<BailCase> bailRelistingNoticeOfHearingCreator,
        @Qualifier("bailNoticeOfHearingConditionalBailRelisting") DocumentCreator<BailCase> bailConditionalBailRelistingNoticeOfHearingCreator,
        BailDocumentHandler bailDocumentHandler
    ) {
        this.bailInitialListingNoticeOfHearingCreator = bailInitialListingNoticeOfHearingCreator;
        this.bailRelistingNoticeOfHearingCreator = bailRelistingNoticeOfHearingCreator;
        this.bailConditionalBailRelistingNoticeOfHearingCreator = bailConditionalBailRelistingNoticeOfHearingCreator;
        this.bailDocumentHandler = bailDocumentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CASE_LISTING;
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
        boolean isInitialListing = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .map(listingEvent -> INITIAL_LISTING == listingEvent).orElse(false);
        boolean isConditionalBail = bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ALL_USERS, String.class)
            .orElse("null").equals(State.DECISION_CONDITIONAL_BAIL.toString());
        Document bailDocument;
        if (isInitialListing) {
            bailDocument = bailInitialListingNoticeOfHearingCreator.create(caseDetails);
        } else if (isConditionalBail) {
            bailDocument = bailConditionalBailRelistingNoticeOfHearingCreator.create(caseDetails);
        } else {
            bailDocument = bailRelistingNoticeOfHearingCreator.create(caseDetails);
        }

        bailDocumentHandler.appendWithMetadata(
            bailCase,
            bailDocument,
            HEARING_DOCUMENTS,
            DocumentTag.BAIL_NOTICE_OF_HEARING
        );

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
