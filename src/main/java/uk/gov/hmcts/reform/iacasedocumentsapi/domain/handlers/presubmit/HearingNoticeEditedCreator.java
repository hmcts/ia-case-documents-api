package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_DOCUMENTS;

import java.util.Optional;
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

@Component
public class HearingNoticeEditedCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hearingNoticeEditedDocumentCreator;
    private final DocumentHandler documentHandler;

    public HearingNoticeEditedCreator(
        @Qualifier("hearingNoticeEdited") DocumentCreator<AsylumCase> hearingNoticeEditedDocumentCreator,
        DocumentHandler documentHandler
    ) {
        this.hearingNoticeEditedDocumentCreator = hearingNoticeEditedDocumentCreator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && asList(Event.EDIT_CASE_LISTING).contains(callback.getEvent());
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
        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        Document hearingNoticeEdited =
            hearingNoticeEditedDocumentCreator.create(
                caseDetails,
                caseDetailsBefore
                    .orElseThrow(() -> new IllegalStateException("previous case data is not present")));

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            hearingNoticeEdited,
            HEARING_DOCUMENTS,
            DocumentTag.HEARING_NOTICE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
