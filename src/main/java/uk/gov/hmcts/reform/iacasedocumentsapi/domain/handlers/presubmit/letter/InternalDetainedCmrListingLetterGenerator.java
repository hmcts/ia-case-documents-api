package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes;

@Slf4j
@Component
public class InternalDetainedCmrListingLetterGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> documentCreator;
    private final DocumentHandler documentHandler;

    public InternalDetainedCmrListingLetterGenerator(
        @Qualifier("internalDetainedCmrListing") DocumentCreator<AsylumCase> documentCreator,
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

        log.info("--------------------------------------");
        log.info(
                "callback.getEvent() == CMR_LISTING || callback.getEvent() == CMR_RE_LISTING: {}",
                callback.getEvent() == CMR_LISTING || callback.getEvent() == CMR_RE_LISTING
        );
        log.info("!isAcceleratedDetainedAppeal(asylumCase): {}", !isAcceleratedDetainedAppeal(asylumCase));
        log.info(
                "isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON): {}",
                isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)
        );
        log.info("--------------------------------------");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == CMR_LISTING || callback.getEvent() == CMR_RE_LISTING)
               && !isAcceleratedDetainedAppeal(asylumCase)
               && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON);
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

        Document internalDetainedListCaseLetter = documentCreator.create(caseDetails);

        log.info("--------------------------------------");
        log.info("internalDetainedListCaseLetter: {}", internalDetainedListCaseLetter.getDocumentFilename());
        log.info("internalDetainedListCaseLetter: {}", internalDetainedListCaseLetter.getDocumentUrl());
        log.info("Adding internal detained list case letter to asylum case INTERNAL_CMR_LISTING_LETTER");
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            internalDetainedListCaseLetter,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_CMR_LISTING_LETTER
        );
        log.info("Added internal detained list case letter to asylum case INTERNAL_CMR_LISTING_LETTER");
        log.info("--------------------------------------");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
