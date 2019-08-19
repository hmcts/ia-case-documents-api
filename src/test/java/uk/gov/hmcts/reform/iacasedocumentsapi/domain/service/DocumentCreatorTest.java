package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
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

        when(documentUploader.upload(documentResource, documentContentType)).thenReturn(expectedDocument);

        Document actualDocument = documentCreator.create(caseDetails);

        assertEquals(expectedDocument, actualDocument);

        verify(documentGenerator, times(1)).generate(any(), any(), any(), any());
        verify(documentUploader, times(1)).upload(any(), any());
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

        when(documentUploader.upload(documentResource, documentContentType)).thenReturn(expectedDocument);

        Document actualDocument = documentCreator.create(caseDetails, caseDetailsBefore);

        assertEquals(expectedDocument, actualDocument);

        verify(documentGenerator, times(1)).generate(any(), any(), any(), any());
        verify(documentUploader, times(1)).upload(any(), any());
    }
}
