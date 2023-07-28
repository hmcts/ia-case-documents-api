package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AsylumCaseFileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter.*;

@Configuration
public class DocumentCreatorConfiguration {

    @Bean("appealSubmission")
    public DocumentCreator<AsylumCase> getAppealSubmissionDocumentCreator(
        @Value("${appealSubmissionDocument.contentType}") String contentType,
        @Value("${appealSubmissionDocument.fileExtension}") String fileExtension,
        @Value("${appealSubmissionDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        AppealSubmissionTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingRequirements")
    public DocumentCreator<AsylumCase> getHearingRequirementsDocumentCreator(
        @Value("${hearingRequirementsDocument.contentType}") String contentType,
        @Value("${hearingRequirementsDocument.fileExtension}") String fileExtension,
        @Value("${hearingRequirementsDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingRequirementsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNotice")
    public DocumentCreator<AsylumCase> getHearingNoticeDocumentCreator(
        @Value("${hearingNoticeDocument.contentType}") String contentType,
        @Value("${hearingNoticeDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        @Qualifier("hearingNoticeTemplate") HearingNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("remoteHearingNotice")
    public DocumentCreator<AsylumCase> getRemoteHearingNoticeDocumentCreator(
        @Value("${remoteHearingNoticeDocument.contentType}") String contentType,
        @Value("${remoteHearingNoticeDocument.fileExtension}") String fileExtension,
        @Value("${remoteHearingNoticeDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        @Qualifier("remoteHearingNoticeTemplate") HearingNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNoticeAdjournedWithoutDate")
    public DocumentCreator<AsylumCase> getHearingNoticeAdjournedWithoutDateDocumentCreator(
        @Value("${hearingNoticeAdjournedWithoutDateDocument.contentType}") String contentType,
        @Value("${hearingNoticeAdjournedWithoutDateDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeAdjournedWithoutDateDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        @Qualifier("hearingNoticeAdjournedWithoutDateTemplate") HearingNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNoticeEdited")
    public DocumentCreator<AsylumCase> getHearingNoticeEditedDocumentCreator(
        @Value("${hearingNoticeEditedDocument.contentType}") String contentType,
        @Value("${hearingNoticeEditedDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeEditedDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingNoticeEditedTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNoticeUpdatedRequirements")
    public DocumentCreator<AsylumCase> getHearingNoticeUpdatedRequirementsDocumentCreator(
        @Value("${hearingNoticeUpdatedRequirementsDocument.contentType}") String contentType,
        @Value("${hearingNoticeUpdatedRequirementsDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeUpdatedRequirementsDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingNoticeUpdatedRequirementsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("hearingNoticeUpdatedDetails")
    public DocumentCreator<AsylumCase> getHearingNoticeUpdatedDetailsDocumentCreator(
        @Value("${hearingNoticeUpdatedDetailsDocument.contentType}") String contentType,
        @Value("${hearingNoticeUpdatedDetailsDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeUpdatedDetailsDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingNoticeUpdatedDetailsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("adaHearingNoticeUpdatedDetails")
    public DocumentCreator<AsylumCase> getAdaHearingNoticeUpdatedDetailsDocumentCreator(
            @Value("${adaHearingNoticeUpdatedDetailsDocument.contentType}") String contentType,
            @Value("${adaHearingNoticeUpdatedDetailsDocument.fileExtension}") String fileExtension,
            @Value("${adaHearingNoticeUpdatedDetailsDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            AdaHearingNoticeUpdatedDetailsTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("remoteHearingNoticeUpdatedDetails")
    public DocumentCreator<AsylumCase> getRemoteHearingNoticeUpdatedDetailsDocumentCreator(
        @Value("${remoteHearingNoticeUpdatedDetailsDocument.contentType}") String contentType,
        @Value("${remoteHearingNoticeUpdatedDetailsDocument.fileExtension}") String fileExtension,
        @Value("${remoteHearingNoticeUpdatedDetailsDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        HearingNoticeUpdatedDetailsRemoteTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("adaHearingNotice")
    public DocumentCreator<AsylumCase> getAdaHearingNoticeDocumentCreator(
        @Value("${hearingNoticeDocument.contentType}") String contentType,
        @Value("${hearingNoticeDocument.fileExtension}") String fileExtension,
        @Value("${hearingNoticeDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        @Qualifier("adaHearingNoticeTemplate") HearingNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("adaSuitability")
    public DocumentCreator<AsylumCase> getAdaSuitabilityDocumentCreator(
            @Value("${adaSuitabilityDocument.contentType}") String contentType,
            @Value("${adaSuitabilityDocument.fileExtension}") String fileExtension,
            @Value("${adaSuitabilityDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            AdaSuitabilityTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("internalAdaSuitabilitySuitable")
    public DocumentCreator<AsylumCase> getInternalAdaSuitabilityLetterSuitableDocumentCreator(
        @Value("${adaInternalSuitabilityReviewSuitableDocument.contentType}") String contentType,
        @Value("${adaInternalSuitabilityReviewSuitableDocument.fileExtension}") String fileExtension,
        @Value("${adaInternalSuitabilityReviewSuitableDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        InternalAdaSuitabilityReviewSuitableLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("internalAdaSuitabilityUnsuitable")
    public DocumentCreator<AsylumCase> getInternalAdaSuitabilityLetterUnsuitableDocumentCreator(
        @Value("${adaInternalSuitabilityReviewUnsuitableDocument.contentType}") String contentType,
        @Value("${adaInternalSuitabilityReviewUnsuitableDocument.fileExtension}") String fileExtension,
        @Value("${adaInternalSuitabilityReviewUnsuitableDocument.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        InternalAdaSuitabilityReviewUnsuitableLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("decisionAndReasons")
    public DocumentCreator<AsylumCase> getAndDecisionAndReasonsDocumentCreator(
        @Value("${decisionAndReasons.contentType}") String contentType,
        @Value("${decisionAndReasons.fileExtension}") String fileExtension,
        @Value("${decisionAndReasons.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        DecisionAndReasonsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("aipDecisionAndReasonsCoverLetter")
    public DocumentCreator<AsylumCase> getAipDecisionAndReasonsCoverLetterDocumentCreator(
        @Value("${aipDecisionAndReasonsCoverLetter.contentType}") String contentType,
        @Value("${aipDecisionAndReasonsCoverLetter.fileExtension}") String fileExtension,
        @Value("${aipDecisionAndReasonsCoverLetter.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        AppellantDecisionAndReasonsCoverLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("decisionAndReasonsCoverLetter")
    public DocumentCreator<AsylumCase> getAndDecisionAndReasonsCoverLetterDocumentCreator(
        @Value("${decisionAndReasonsCoverLetter.contentType}") String contentType,
        @Value("${decisionAndReasonsCoverLetter.fileExtension}") String fileExtension,
        @Value("${decisionAndReasonsCoverLetter.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        DecisionAndReasonsCoverLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("endAppealNotice")
    public DocumentCreator<AsylumCase> getEndAppealDocumentCreator(
        @Value("${endAppeal.contentType}") String contentType,
        @Value("${endAppeal.fileExtension}") String fileExtension,
        @Value("${endAppeal.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        EndAppealTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("endAppealAppellantNotice")
    public DocumentCreator<AsylumCase> getEndAppealAppellantDocumentCreator(
        @Value("${endAppeal.contentType}") String contentType,
        @Value("${endAppeal.fileExtension}") String fileExtension,
        @Value("${endAppeal.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        EndAppealAppellantTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("endAppealAutomaticallyNotice")
    public DocumentCreator<AsylumCase> getEndAppealAutomaticallyDocumentCreator(
        @Value("${endAppealAutomatically.contentType}") String contentType,
        @Value("${endAppealAutomatically.fileExtension}") String fileExtension,
        @Value("${endAppealAutomatically.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        EndAppealAutomaticallyTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("cmaRequirements")
    public DocumentCreator<AsylumCase> getCmaRequirementsDocumentCreator(
        @Value("${cmaRequirements.contentType}") String contentType,
        @Value("${cmaRequirements.fileExtension}") String fileExtension,
        @Value("${cmaRequirements.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        CmaRequirementsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("cmaAppointmentNotice")
    public DocumentCreator<AsylumCase> getCmaAppointmentNoticeDocumentCreator(
        @Value("${cmaAppointmentNotice.contentType}") String contentType,
        @Value("${cmaAppointmentNotice.fileExtension}") String fileExtension,
        @Value("${cmaAppointmentNotice.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        CmaAppointmentNoticeTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("appealReasons")
    public DocumentCreator<AsylumCase> getAppealReasonsDocumentCreator(
        @Value("${appealReasons.contentType}") String contentType,
        @Value("${appealReasons.fileExtension}") String fileExtension,
        @Value("${appealReasons.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        AppealReasonsTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("clarifyingQuestionsAnswers")
    public DocumentCreator<AsylumCase> getAppealReasonsDocumentCreator(
        @Value("${clarifyingQuestionsAnswers.contentType}") String contentType,
        @Value("${clarifyingQuestionsAnswers.fileExtension}") String fileExtension,
        @Value("${clarifyingQuestionsAnswers.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        ClarifyingQuestionsAnswersTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("internalAdaRequestBuildCase")
    public DocumentCreator<AsylumCase> getInternalAdaBuildCaseDocumentCreator(
            @Value("${internalAdaRequestBuildCaseDocument.contentType}") String contentType,
            @Value("${internalAdaRequestBuildCaseDocument.fileExtension}") String fileExtension,
            @Value("${internalAdaRequestBuildCaseDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalAdaRequestBuildCaseTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("internalDetainedRequestBuildCase")
    public DocumentCreator<AsylumCase> getInternalDetainedBuildCaseDocumentCreator(
            @Value("${internalDetainedRequestBuildCaseDocument.contentType}") String contentType,
            @Value("${internalDetainedRequestBuildCaseDocument.fileExtension}") String fileExtension,
            @Value("${internalDetainedRequestBuildCaseDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalDetainedRequestBuildCaseTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("hoReviewEvidenceLetter")
    public DocumentCreator<AsylumCase> getHoReviewEvidenceLetterCreator(
            @Value("${hoReviewEvidenceLetter.contentType}") String contentType,
            @Value("${hoReviewEvidenceLetter.fileExtension}") String fileExtension,
            @Value("${hoReviewEvidenceLetter.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            HoReviewEvidenceLetterTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("internalDetainedDecisionsAndReasonsAllowed")
    public DocumentCreator<AsylumCase> getInternalDetainedDecisionsAndReasonsAllowedDocumentCreator(
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.contentType}") String contentType,
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.fileExtension}") String fileExtension,
        @Value("${internalDetainedDecisionsAndReasonsAllowedLetter.fileName}") String fileName,
        AsylumCaseFileNameQualifier fileNameQualifier,
        InternalDetainedDecisionsAndReasonsAllowedLetterTemplate documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
            contentType,
            fileExtension,
            fileName,
            fileNameQualifier,
            documentTemplate,
            documentGenerator,
            documentUploader
        );
    }

    @Bean("internalAdaDecisionsAndReasonsDismissed")
    public DocumentCreator<AsylumCase> getInternalAdaDecisionsAndReasonsDismissedDocumentCreator(
            @Value("${internalAdaDecisionsAndReasonsDismissedLetter.contentType}") String contentType,
            @Value("${internalAdaDecisionsAndReasonsDismissedLetter.fileExtension}") String fileExtension,
            @Value("${internalAdaDecisionsAndReasonsDismissedLetter.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalAdaDecisionsAndReasonsDismissedLetterTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("uploadTheAppealResponseLetter")
    public DocumentCreator<AsylumCase> getUploadAppealResponseMaintainedLetterCreator(
            @Value("${uploadAppealResponseMaintainedLetter.contentType}") String contentType,
            @Value("${uploadAppealResponseMaintainedLetter.fileExtension}") String fileExtension,
            @Value("${uploadAppealResponseMaintainedLetter.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            UploadAppealResponseMaintainedDecisionLetterTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("internalDetHearingBundle")
    public DocumentCreator<AsylumCase> getInternalDetHearingBundleDocumentCreator(
            @Value("${internalAdaHearingBundle.contentType}") String contentType,
            @Value("${internalAdaHearingBundle.fileExtension}") String fileExtension,
            @Value("${internalAdaHearingBundle.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalDetGenerateHearingBundleTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("internalAppealSubmission")
    public DocumentCreator<AsylumCase> getInternalAppealSubmissionDocumentCreator(
            @Value("${internalAppealSubmissionDocument.contentType}") String contentType,
            @Value("${internalAppealSubmissionDocument.fileExtension}") String fileExtension,
            @Value("${internalAppealSubmissionDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalAppealSubmissionTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

    @Bean("internalAppealCanProceedLetter")
    public DocumentCreator<AsylumCase> getInternalAppealCanProceedLetterCreator(
            @Value("${internalAppealCanProceedDocument.contentType}") String contentType,
            @Value("${internalAppealCanProceedDocument.fileExtension}") String fileExtension,
            @Value("${internalAppealCanProceedDocument.fileName}") String fileName,
            AsylumCaseFileNameQualifier fileNameQualifier,
            InternalAppealCanProceedLetterTemplate documentTemplate,
            DocumentGenerator documentGenerator,
            DocumentUploader documentUploader
    ) {
        return new DocumentCreator<>(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
        );
    }

}
