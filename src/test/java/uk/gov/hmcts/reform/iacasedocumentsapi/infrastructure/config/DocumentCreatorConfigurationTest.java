package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.AsylumCaseFileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.CmrHearingNoticeTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter.InternalCmrListingAppellantLetterTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter.InternalCmrListingLetterTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter.InternalCmrListingLrLetterTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter.InternalCmrReListingLetterTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter.InternalDetainedCmrListingLetterTemplate;

@ExtendWith(MockitoExtension.class)
class DocumentCreatorConfigurationTest {

    @Mock private AsylumCaseFileNameQualifier fileNameQualifier;
    @Mock private CmrHearingNoticeTemplate cmrHearingNoticeTemplate;
    @Mock private InternalDetainedCmrListingLetterTemplate internalDetainedCmrListingLetterTemplate;
    @Mock private InternalCmrListingLetterTemplate internalCmrListingLetterTemplate;
    @Mock private InternalCmrListingLrLetterTemplate internalCmrListingLrLetterTemplate;
    @Mock private InternalCmrListingAppellantLetterTemplate internalCmrListingAppellantLetterTemplate;
    @Mock private InternalCmrReListingLetterTemplate internalCmrReListingLetterTemplate;
    @Mock private DocumentGenerator documentGenerator;
    @Mock private DocumentUploader documentUploader;

    private final String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final String fileExtension = "docx";
    private final String fileName = "cmr-hearing-notice";

    private DocumentCreatorConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new DocumentCreatorConfiguration();
    }

    @Test
    void should_create_cmr_hearing_notice_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getCmrHearingNoticeDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                cmrHearingNoticeTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_remote_cmr_hearing_notice_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getRemoteCmrHearingNoticeDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                cmrHearingNoticeTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_internal_detained_cmr_listing_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getInternalDetainedCmrListingDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalDetainedCmrListingLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_internal_cmr_listing_letter_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getinternalCmrListingLetterDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalCmrListingLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_internal_cmr_listing_lr_letter_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getInternalCmrListingLrLetterDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalCmrListingLrLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_internal_cmr_listing_appellant_letter_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getInternalCmrListingAppellantLetterDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalCmrListingAppellantLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_internal_cmr_re_listing_letter_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getInternalCmrReListingLetterDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalCmrReListingLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }

    @Test
    void should_create_cmr_listing_detained_other_appellant_letter_document_creator() {
        DocumentCreator<AsylumCase> documentCreator =
            configuration.getCmrListingDetainedOtherAppellantLetterDocumentCreator(
                contentType,
                fileExtension,
                fileName,
                fileNameQualifier,
                internalCmrListingLetterTemplate,
                documentGenerator,
                documentUploader
            );

        assertThat(documentCreator).isNotNull();
    }
}
