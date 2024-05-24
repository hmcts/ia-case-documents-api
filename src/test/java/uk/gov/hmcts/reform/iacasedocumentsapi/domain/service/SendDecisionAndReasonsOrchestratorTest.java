package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReheardHearingDocuments;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;

@ExtendWith(MockitoExtension.class)
class SendDecisionAndReasonsOrchestratorTest {

    @Mock private DocumentHandler documentHandler;
    @Mock private SendDecisionAndReasonsRenameFileService sendDecisionAndReasonsPdfService;
    @Mock private SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document coverLetter;
    @Mock private Document pdf;
    @Mock private FeatureToggler featureToggler;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Appender<ReheardHearingDocuments> reheardAppender;

    private SendDecisionAndReasonsOrchestrator sendDecisionAndReasonsOrchestrator;

    @BeforeEach
    public void setUp() {
        sendDecisionAndReasonsOrchestrator =
            new SendDecisionAndReasonsOrchestrator(
                documentHandler,
                sendDecisionAndReasonsPdfService,
                sendDecisionAndReasonsCoverLetterService,
                featureToggler,
                documentReceiver,
                documentsAppender,
                reheardAppender);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void throws_and_skips_document_update_if_cover_letter_null() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails)).thenReturn(null);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .hasMessage("Cover letter creation failed")
            .isExactlyInstanceOf(NullPointerException.class);

        verifyNoInteractions(documentHandler);
        verifyNoInteractions(sendDecisionAndReasonsPdfService);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    void throws_and_skips_document_update_if_cover_letter_fails_with_exception() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails)).thenThrow(
            DocumentServiceResponseException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .isExactlyInstanceOf(DocumentServiceResponseException.class);

        verifyNoInteractions(documentHandler);
        verifyNoInteractions(sendDecisionAndReasonsPdfService);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    void throws_and_fails_with_exception_when_decision_and_reasons_file_is_null() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .thenReturn(null);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .hasMessage("Document to pdf conversion failed")
            .isExactlyInstanceOf(NullPointerException.class);

        verifyNoInteractions(documentHandler);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    void throws_and_fails_with_exception_when_pdf_generation_throws() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(documentHandler);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    void attaches_new_documents_and_when_cover_letter_generated_and_pdf_name_updated() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .thenReturn(pdf);

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                pdf,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF
            );

        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
        verify(asylumCase, times(1)).clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
    }

    @Test
    void attaches_new_documents_and_when_cover_letter_and_pdf_generated_for_normal_case() {

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .thenReturn(pdf);

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                pdf,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF
            );

        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
        verify(asylumCase, times(1)).clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(DRAFT_REHEARD_DECISION_AND_REASONS);
    }

    @Test
    void attaches_new_reheard_documents_and_when_cover_letter_and_pdf_generated_for_reheard_case() {

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .thenReturn(pdf);

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                REHEARD_DECISION_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                REHEARD_DECISION_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                pdf,
                REHEARD_DECISION_REASONS_DOCUMENTS,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF
            );

        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
        verify(asylumCase, times(1)).clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
    }

    @Test
    void attaches_new_reheard_documents_complex_collection_when_reheard() {

        String description = "Some evidence";
        String dateUploaded = "2018-12-25";
        final DocumentWithMetadata coverLetterDocumentWithMetadata =
                new DocumentWithMetadata(
                        coverLetter,
                        description,
                        dateUploaded,
                        DocumentTag.DECISION_AND_REASONS_COVER_LETTER,
                        "test"
                );

        final DocumentWithMetadata decisionDocumentWithMetadata =
                new DocumentWithMetadata(
                        coverLetter,
                        description,
                        dateUploaded,
                        DocumentTag.FINAL_DECISION_AND_REASONS_PDF,
                        "test"
                );

        IdValue<DocumentWithMetadata> decisionDocWithMetadata =
                new IdValue<>("2", decisionDocumentWithMetadata);
        IdValue<DocumentWithMetadata> coverLetterDocWithMetadata =
                new IdValue<>("1", coverLetterDocumentWithMetadata);
        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(decisionDocWithMetadata, coverLetterDocWithMetadata);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
                new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        final List<IdValue<ReheardHearingDocuments>> listOfReheardDocs = Lists.newArrayList(reheardHearingDocuments);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails)).thenReturn(coverLetter);
        when(sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails)).thenReturn(pdf);
        when(documentReceiver.receive(coverLetter, "", DocumentTag.DECISION_AND_REASONS_COVER_LETTER))
                .thenReturn(coverLetterDocumentWithMetadata);
        when(documentReceiver.receive(pdf, "", DocumentTag.FINAL_DECISION_AND_REASONS_PDF))
                .thenReturn(decisionDocumentWithMetadata);
        when(documentsAppender.append(Collections.emptyList(), List.of(decisionDocumentWithMetadata, coverLetterDocumentWithMetadata)))
                .thenReturn(listOfDocumentsWithMetadata);
        when(asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION)).thenReturn(Optional.of(Collections.emptyList()));
        when(reheardAppender.append(any(ReheardHearingDocuments.class), anyList())).thenReturn(listOfReheardDocs);

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);

        verify(documentReceiver, times(1)).receive(coverLetter, "",
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER);
        verify(documentReceiver, times(1)).receive(pdf, "",
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF);
        verify(documentsAppender, times(1)).append(Collections.emptyList(),
                List.of(decisionDocumentWithMetadata, coverLetterDocumentWithMetadata));
        verify(reheardAppender, times(1)).append(any(ReheardHearingDocuments.class), anyList());
        verify(asylumCase, times(1)).write(REHEARD_DECISION_REASONS_COLLECTION, listOfReheardDocs);
    }
}
