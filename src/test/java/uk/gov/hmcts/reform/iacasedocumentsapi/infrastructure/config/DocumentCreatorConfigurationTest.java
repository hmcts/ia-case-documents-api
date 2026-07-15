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

@ExtendWith(MockitoExtension.class)
class DocumentCreatorConfigurationTest {

    @Mock private AsylumCaseFileNameQualifier fileNameQualifier;
    @Mock private CmrHearingNoticeTemplate cmrHearingNoticeTemplate;
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
}
