package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;


@Component
public class EndAppealNoticeCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> endAppealNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> endAppealAppellantNoticeDocumentCreator;
    private final DocumentHandler documentHandler;

    public EndAppealNoticeCreator(
        @Qualifier("endAppealNotice") DocumentCreator<AsylumCase> endAppealNoticeDocumentCreator,
        @Qualifier("endAppealAppellantNotice") DocumentCreator<AsylumCase> endAppealAppellantNoticeDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.endAppealNoticeDocumentCreator = endAppealNoticeDocumentCreator;
        this.endAppealAppellantNoticeDocumentCreator = endAppealAppellantNoticeDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.END_APPEAL;
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

        boolean isAipJourney = asylumCase
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == AIP).orElse(false);

        Document endAppealNotice;

        if (isAipJourney || hasAppealBeenSubmittedByAppellantInternalCase(asylumCase)) {
            endAppealNotice = endAppealAppellantNoticeDocumentCreator.create(caseDetails);
        } else {
            endAppealNotice = endAppealNoticeDocumentCreator.create(caseDetails);
        }

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            endAppealNotice,
            TRIBUNAL_DOCUMENTS,
            DocumentTag.END_APPEAL
        );

        if (isInternalNonDetainedCase(asylumCase) && hasAppellantAddressInCountryOrOoc(asylumCase)) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    endAppealNotice,
                    LETTER_NOTIFICATION_DOCUMENTS,
                    DocumentTag.INTERNAL_END_APPEAL_LETTER
            );
        }

        if ((hasAppealBeenSubmittedByAppellantInternalCase(asylumCase) && isDetainedInFacilityType(asylumCase, OTHER)) && hasAppellantAddressInCountryOrOoc(asylumCase)) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    endAppealNotice,
                    LETTER_NOTIFICATION_DOCUMENTS,
                    DocumentTag.INTERNAL_END_APPEAL_LETTER
            );
        }

        if ((hasAppealBeenSubmittedByAppellantInternalCase(asylumCase) && isDetainedInOneOfFacilityTypes(asylumCase, IRC, PRISON)) && hasAppellantAddressInCountryOrOoc(asylumCase)) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    endAppealNotice,
                    NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.END_APPEAL
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
