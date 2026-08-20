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
    private final DocumentCreator<AsylumCase> cmrRelistedHearingNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteCmrRelistedHearingNoticeDocumentCreator;
    private final DocumentHandler documentHandler;

    public CmrListingHearingNoticeCreator(
            @Qualifier("cmrHearingNotice") DocumentCreator<AsylumCase> cmrHearingNoticeDocumentCreator,
            @Qualifier("cmrRelistedHearingNotice") DocumentCreator<AsylumCase> cmrRelistedHearingNoticeDocumentCreator,
            @Qualifier("remoteCmrHearingNotice") DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator,
            @Qualifier("remoteCmrRelistedHearingNotice") DocumentCreator<AsylumCase> remoteCmrRelistedHearingNoticeDocumentCreator,
            DocumentHandler documentHandler
    ) {
        this.cmrHearingNoticeDocumentCreator = cmrHearingNoticeDocumentCreator;
        this.cmrRelistedHearingNoticeDocumentCreator = cmrRelistedHearingNoticeDocumentCreator;
        this.remoteCmrHearingNoticeDocumentCreator = remoteCmrHearingNoticeDocumentCreator;
        this.remoteCmrRelistedHearingNoticeDocumentCreator = remoteCmrRelistedHearingNoticeDocumentCreator;
        this.documentHandler = documentHandler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (Event.CMR_LISTING.equals(callback.getEvent())
                || Event.CMR_RE_LISTING.equals(callback.getEvent()));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final boolean isReListing = Event.CMR_RE_LISTING.equals(callback.getEvent());

        Document hearingNotice = getHearingNotice(asylumCase, caseDetails, callback);

        documentHandler.addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                HEARING_DOCUMENTS,
                DocumentTag.HEARING_NOTICE
        );

        addDetainedCaseDocument(asylumCase, hearingNotice);
        addInternalCaseDocument(asylumCase, hearingNotice, isReListing);
        addRemoteCmrDocument(asylumCase, hearingNotice, isReListing);
        addLegalRepresentativeDocument(asylumCase, hearingNotice, isReListing);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addDetainedCaseDocument(
            AsylumCase asylumCase,
            Document hearingNotice
    ) {
        if (isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    hearingNotice,
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_CMR_LISTING_LETTER
            );
        }
    }

    private void addInternalCaseDocument(
            AsylumCase asylumCase,
            Document hearingNotice,
            boolean isReListing
    ) {
        if (isInternalNonDetainedCase(asylumCase)
                || (isInternalCase(asylumCase) && isDetainedInFacilityType(asylumCase, OTHER))) {

            addLetterNotificationDocument(
                    asylumCase,
                    hearingNotice,
                    isReListing
                            ? DocumentTag.INTERNAL_CMR_RE_LISTING_LETTER
                            : DocumentTag.INTERNAL_CMR_LISTING_LETTER
            );
        }
    }

    private void addRemoteCmrDocument(
            AsylumCase asylumCase,
            Document hearingNotice,
            boolean isReListing
    ) {
        if (!isRemoteCmrHearing(asylumCase)) {
            return;
        }

        boolean isLegalRepresentative =
                hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);

        DocumentTag documentTag;

        if (isReListing) {
            documentTag = isLegalRepresentative
                    ? DocumentTag.REMOTE_CMR_RE_LISTING_LR_LETTER
                    : DocumentTag.REMOTE_CMR_RE_LISTING_LETTER;
        } else {
            documentTag = isLegalRepresentative
                    ? DocumentTag.REMOTE_CMR_LISTING_LR_LETTER
                    : DocumentTag.REMOTE_CMR_LISTING_LETTER;
        }

        addLetterNotificationDocument(asylumCase, hearingNotice, documentTag);
    }

    private void addLegalRepresentativeDocument(
            AsylumCase asylumCase,
            Document hearingNotice,
            boolean isReListing
    ) {
        if (!hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            return;
        }

        addLetterNotificationDocument(
                asylumCase,
                hearingNotice,
                isReListing
                        ? DocumentTag.INTERNAL_CMR_RE_LISTING_LR_LETTER
                        : DocumentTag.INTERNAL_CMR_LISTING_LR_LETTER
        );
    }

    private void addLetterNotificationDocument(
            AsylumCase asylumCase,
            Document hearingNotice,
            DocumentTag documentTag
    ) {
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                LETTER_NOTIFICATION_DOCUMENTS,
                documentTag
        );
    }

    private Document getHearingNotice(
            AsylumCase asylumCase,
            CaseDetails<AsylumCase> caseDetails,
            Callback<AsylumCase> callback
    ) {
        boolean isReListing = Event.CMR_RE_LISTING.equals(callback.getEvent());
        boolean isRemote = isRemoteCmrHearing(asylumCase);

        if (isReListing) {
            CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore()
                    .orElseThrow(() -> new IllegalStateException(
                            "Case details before are not present for CMR re-listing"
                    ));

            return isRemote
                    ? remoteCmrRelistedHearingNoticeDocumentCreator.create(caseDetails, caseDetailsBefore)
                    : cmrRelistedHearingNoticeDocumentCreator.create(caseDetails, caseDetailsBefore);
        }

        return isRemote
                ? remoteCmrHearingNoticeDocumentCreator.create(caseDetails)
                : cmrHearingNoticeDocumentCreator.create(caseDetails);
    }
}