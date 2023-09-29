package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;


@Component
public class InternalUploadAdditionalEvidenceGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String LEGAL_OFFICER_ADDENDUM_UPLOADED_BY_LABEL = "TCW";
    private static final String LEGAL_OFFICER_ADDENDUM_SUPPLIED_BY_LABEL = "The respondent";
    private final DocumentCreator<AsylumCase> adminUploadEvidence;
    private final DocumentCreator<AsylumCase> homeOfficeUploadEvidence;
    private final DocumentCreator<AsylumCase> legalOfficereUploadEvidence;
    private final DocumentHandler documentHandler;

    public InternalUploadAdditionalEvidenceGenerator(
        // First DocumentCreator bean represents admin upload however cannot rename as it will require too much refactoring
        @Qualifier("internalUploadAdditionalEvidenceLetter") DocumentCreator<AsylumCase> adminUploadEvidence,
        @Qualifier("internalHomeOfficeUploadAdditionalAddendumEvidenceLetter") DocumentCreator<AsylumCase> homeOfficeUploadEvidence,
        @Qualifier("internalLegalOfficerUploadAdditionalEvidenceLetter") DocumentCreator<AsylumCase> legalOfficereUploadEvidence,
        DocumentHandler documentHandler
    ) {
        this.adminUploadEvidence = adminUploadEvidence;
        this.homeOfficeUploadEvidence = homeOfficeUploadEvidence;
        this.legalOfficereUploadEvidence = legalOfficereUploadEvidence;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Event event = callback.getEvent();

        Optional<IdValue<DocumentWithMetadata>> latestAddendum;

        if (event.equals(UPLOAD_ADDENDUM_EVIDENCE)) {
            latestAddendum = getLatestAddendumEvidenceDocument(asylumCase);
            if (latestAddendum.isEmpty()) {
                return false;
            }

            DocumentWithMetadata addendum = latestAddendum.get().getValue();

            if (addendum.getSuppliedBy() == null || addendum.getUploadedBy() == null) {
                return false;
            }

            if (!addendum.getUploadedBy().equals(LEGAL_OFFICER_ADDENDUM_UPLOADED_BY_LABEL) ||
                    !addendum.getSuppliedBy().equals(LEGAL_OFFICER_ADDENDUM_SUPPLIED_BY_LABEL)) {
                return false;
            }
        }

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && List.of(
                   UPLOAD_ADDITIONAL_EVIDENCE,
                   UPLOAD_ADDENDUM_EVIDENCE,
                   UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
                   UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                   UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE)
                .contains(event)
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

        Document documentToUpload;
        DocumentTag documentTagForUpload;

        if (List.of(UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE, UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE).contains(callback.getEvent())) {
            documentToUpload = homeOfficeUploadEvidence.create(caseDetails);
            documentTagForUpload = HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER;
        } else if (callback.getEvent().equals(UPLOAD_ADDENDUM_EVIDENCE)) {
            documentToUpload = legalOfficereUploadEvidence.create(caseDetails);
            documentTagForUpload = LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER;
        } else {
            documentToUpload = adminUploadEvidence.create(caseDetails);
            documentTagForUpload = DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER;
        }

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            documentToUpload,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            documentTagForUpload
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
