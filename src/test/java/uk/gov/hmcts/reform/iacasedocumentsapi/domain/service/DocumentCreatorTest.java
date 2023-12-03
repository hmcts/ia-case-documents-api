package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DocumentCreatorTest {

    private final String documentContentType = "application/pdf";
    private final String documentFileExtension = "PDF";
    private final String documentFileName = "some-document";

    @Mock private FileNameQualifier<CaseData> fileNameQualifier;
    @Mock private DocumentTemplate<CaseData> documentTemplate;
    @Mock private DocumentGenerator documentGenerator;
    @Mock private DocumentUploader documentUploader;

    @Mock private CaseDetails<CaseData> caseDetails;
    @Mock private CaseDetails<CaseData> caseDetailsBefore;
    private String qualifiedDocumentFileName = "unique-to-appellant-some-document";
    private String templateName = "template.docx";
    @Mock private Map<String, Object> templateFieldValues;
    @Mock private Resource documentResource;
    @Mock private Document expectedDocument;

    private DocumentCreator documentCreator;

    @BeforeEach
    public void setUp() {

        documentCreator =
            new DocumentCreator(
                documentContentType,
                documentFileExtension,
                documentFileName,
                fileNameQualifier,
                documentTemplate,
                documentGenerator,
                documentUploader
            );
    }

    @Test
    public void should_orchestrate_document_creation() {

        when(fileNameQualifier.get(documentFileName, caseDetails)).thenReturn(qualifiedDocumentFileName);
        when(documentTemplate.getName()).thenReturn(templateName);
        when(documentTemplate.mapFieldValues(caseDetails)).thenReturn(templateFieldValues);

        when(documentGenerator.generate(
            qualifiedDocumentFileName,
            documentFileExtension,
            templateName,
            templateFieldValues
        )).thenReturn(documentResource);

        when(documentUploader.upload(
                eq(documentResource),
                anyString(),
                anyString(),
                anyString(),
                eq(documentContentType)
        )).thenReturn(expectedDocument);

        Document actualDocument = documentCreator.create(caseDetails);

        assertEquals(expectedDocument, actualDocument);

        verify(documentGenerator, times(1)).generate(any(), any(), any(), any());
        verify(documentUploader, times(1)).upload(
                any(),
                anyString(),
                anyString(),
                anyString(),
                any()
        );
    }

    @Test
    public void should_orchestrate_amended_document_creation() {

        when(fileNameQualifier.get(documentFileName, caseDetails)).thenReturn(qualifiedDocumentFileName);
        when(documentTemplate.getName()).thenReturn(templateName);
        when(documentTemplate.mapFieldValues(caseDetails, caseDetailsBefore)).thenReturn(templateFieldValues);

        when(documentGenerator.generate(
            qualifiedDocumentFileName,
            documentFileExtension,
            templateName,
            templateFieldValues
        )).thenReturn(documentResource);

        when(documentUploader.upload(
                eq(documentResource),
                anyString(),
                anyString(),
                anyString(),
                eq(documentContentType)
        )).thenReturn(expectedDocument);

        Document actualDocument = documentCreator.create(caseDetails, caseDetailsBefore);

        assertEquals(expectedDocument, actualDocument);

        verify(documentGenerator, times(1)).generate(any(), any(), any(), any());
        verify(documentUploader, times(1)).upload(
                any(),
                anyString(),
                anyString(),
                anyString(),
                any()
        );
    }
}
