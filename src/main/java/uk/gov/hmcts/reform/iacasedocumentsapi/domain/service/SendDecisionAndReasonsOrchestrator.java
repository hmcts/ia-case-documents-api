package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReheardHearingDocuments;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@Service
public class SendDecisionAndReasonsOrchestrator {

    private final DocumentHandler documentHandler;
    private final SendDecisionAndReasonsRenameFileService sendDecisionAndReasonsPdfService;
    private final SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;
    private final FeatureToggler featureToggler;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final Appender<ReheardHearingDocuments> reheardHearingAppender;

    public SendDecisionAndReasonsOrchestrator(
            DocumentHandler documentHandler,
            SendDecisionAndReasonsRenameFileService sendDecisionAndReasonsPdfService,
            SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService,
            FeatureToggler featureToggler,
            DocumentReceiver documentReceiver,
            DocumentsAppender documentsAppender,
            Appender<ReheardHearingDocuments> reheardHearingAppender
    ) {
        this.documentHandler = documentHandler;
        this.sendDecisionAndReasonsPdfService = sendDecisionAndReasonsPdfService;
        this.sendDecisionAndReasonsCoverLetterService = sendDecisionAndReasonsCoverLetterService;
        this.featureToggler = featureToggler;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.reheardHearingAppender = reheardHearingAppender;
    }

    public void sendDecisionAndReasons(CaseDetails<AsylumCase> caseDetails) {

        AsylumCase asylumCase = caseDetails.getCaseData();

        try {

            Document coverLetter = requireNonNull(
                sendDecisionAndReasonsCoverLetterService.create(caseDetails),
                "Cover letter creation failed");

            Document finalDecisionAndReasonsPdf =
                sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails);

            requireNonNull(finalDecisionAndReasonsPdf, "Document to pdf conversion failed");

            if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YesOrNo.YES))
                 && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)))) {

                if (featureToggler.getValue("dlrm-remitted-feature-flag", false)) {
                    appendReheardHearingDocuments(asylumCase, coverLetter, finalDecisionAndReasonsPdf);
                } else {
                    documentHandler.addWithMetadata(
                            asylumCase,
                            coverLetter,
                            REHEARD_DECISION_REASONS_DOCUMENTS,
                            DocumentTag.DECISION_AND_REASONS_COVER_LETTER
                    );

                    documentHandler.addWithMetadata(
                            asylumCase,
                            finalDecisionAndReasonsPdf,
                            REHEARD_DECISION_REASONS_DOCUMENTS,
                            DocumentTag.FINAL_DECISION_AND_REASONS_PDF
                    );
                }
            } else {

                documentHandler.addWithMetadata(
                    asylumCase,
                    coverLetter,
                    FINAL_DECISION_AND_REASONS_DOCUMENTS,
                    DocumentTag.DECISION_AND_REASONS_COVER_LETTER
                );

                documentHandler.addWithMetadata(
                    asylumCase,
                    finalDecisionAndReasonsPdf,
                    FINAL_DECISION_AND_REASONS_DOCUMENTS,
                    DocumentTag.FINAL_DECISION_AND_REASONS_PDF
                );
            }

            asylumCase.clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
            asylumCase.clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
            asylumCase.clear(DRAFT_REHEARD_DECISION_AND_REASONS);

        } catch (RuntimeException e) {

            asylumCase.clear(DECISION_AND_REASONS_COVER_LETTER);
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);

            throw e;
        }
    }

    private void appendReheardHearingDocuments(AsylumCase asylumCase, Document coverLetter, Document finalDecisionAndReasonsPdf) {
        DocumentWithMetadata coverLetterWithMetadata =
                documentReceiver.receive(
                        coverLetter,
                        "",
                        DocumentTag.DECISION_AND_REASONS_COVER_LETTER
                );

        DocumentWithMetadata decisionWithMetadata =
                documentReceiver.receive(
                        finalDecisionAndReasonsPdf,
                        "",
                        DocumentTag.FINAL_DECISION_AND_REASONS_PDF
                );

        List<IdValue<DocumentWithMetadata>> allDocuments =
                documentsAppender.append(
                        Collections.emptyList(),
                        List.of(decisionWithMetadata, coverLetterWithMetadata)
                );

        ReheardHearingDocuments newReheardDocuments = new ReheardHearingDocuments(allDocuments);

        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
                asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION);
        List<IdValue<ReheardHearingDocuments>> allReheardDocuments =
                reheardHearingAppender.append(newReheardDocuments, maybeExistingReheardDocuments.orElse(emptyList()));
        asylumCase.write(REHEARD_DECISION_REASONS_COLLECTION, allReheardDocuments);
    }
}
