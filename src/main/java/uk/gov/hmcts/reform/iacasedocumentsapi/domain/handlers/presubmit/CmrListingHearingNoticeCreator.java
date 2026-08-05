package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.*;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Component
public class CmrListingHearingNoticeCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> cmrHearingNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator;
    private final DocumentHandler documentHandler;

    public CmrListingHearingNoticeCreator(
        @Qualifier("cmrHearingNotice") DocumentCreator<AsylumCase> cmrHearingNoticeDocumentCreator,
        @Qualifier("remoteCmrHearingNotice") DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.cmrHearingNoticeDocumentCreator = cmrHearingNoticeDocumentCreator;
        this.remoteCmrHearingNoticeDocumentCreator = remoteCmrHearingNoticeDocumentCreator;
        this.documentHandler = documentHandler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return  DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (Event.CMR_LISTING.equals(callback.getEvent()) || Event.CMR_RE_LISTING.equals(callback.getEvent()));
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

        Document hearingNotice;

        hearingNotice = getHearingNotice(asylumCase, caseDetails);
        documentHandler.addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            hearingNotice,
            HEARING_DOCUMENTS,
            DocumentTag.HEARING_NOTICE
        );

        if (isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_CMR_LISTING_LETTER
            );
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER
            );
        }

        if (isInternalNonDetainedCase(asylumCase)
            || (isInternalCase(asylumCase) && isDetainedInFacilityType(asylumCase, OTHER))) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                LETTER_NOTIFICATION_DOCUMENTS,
                DocumentTag.INTERNAL_CMR_LISTING_LETTER
            );
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                LETTER_NOTIFICATION_DOCUMENTS,
                DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Document getHearingNotice(
        AsylumCase asylumCase,
        CaseDetails<AsylumCase> caseDetails
    ) {
        Document hearingNotice;
        if (isRemoteCmrHearing(asylumCase)) {
            hearingNotice = remoteCmrHearingNoticeDocumentCreator.create(caseDetails);
        } else {
            hearingNotice = cmrHearingNoticeDocumentCreator.create(caseDetails);
        }
        return hearingNotice;
    }
}
