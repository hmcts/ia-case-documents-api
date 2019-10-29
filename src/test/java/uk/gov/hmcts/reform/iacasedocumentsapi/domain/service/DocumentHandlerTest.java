package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
public class DocumentHandlerTest {

    @Mock private AsylumCase asylumCase;
    @Mock private Document document;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private DocumentReceiver documentReceiver;

    @InjectMocks private DocumentHandler documentHandler;

    private final DocumentTag tag = DocumentTag.ADDITIONAL_EVIDENCE;
    private final AsylumCaseDefinition documentField = AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

    @Test
    public void should_add_document_to_empty_list() {
        DocumentWithMetadata documentWithMetadata = createDocumentWithMetadata();
        List<IdValue<DocumentWithMetadata>> documents = newArrayList();
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(new IdValue<>("1", documentWithMetadata));

        when(asylumCase.read(documentField))
            .thenReturn(Optional.of(documents));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(documentWithMetadata);
        when(documentsAppender.append(documents, Collections.singletonList(documentWithMetadata), tag))
            .thenReturn(allDocuments);

        documentHandler.addWithMetadata(
            asylumCase,
            document,
            documentField,
            tag
        );

        verify(asylumCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(documents, Collections.singletonList(documentWithMetadata), tag);
        verify(asylumCase).write(documentField, allDocuments);
    }

    @Test
    public void should_add_document_to_non_empty_list() {
        DocumentWithMetadata documentWithMetadata = createDocumentWithMetadata();
        List<IdValue<DocumentWithMetadata>> documents = newArrayList(new IdValue<>("1", createDocumentWithMetadata()));
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(documents.get(0), new IdValue<>("2", documentWithMetadata));

        when(asylumCase.read(documentField))
            .thenReturn(Optional.of(documents));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(documentWithMetadata);
        when(documentsAppender.append(documents, Collections.singletonList(documentWithMetadata), tag))
            .thenReturn(allDocuments);

        documentHandler.addWithMetadata(
            asylumCase,
            document,
            documentField,
            tag
        );

        verify(asylumCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(documents, Collections.singletonList(documentWithMetadata), tag);
        verify(asylumCase).write(documentField, allDocuments);
    }

    @Test
    public void should_add_document_to_non_empty_list_without_replacing_documents() {

        DocumentWithMetadata newDocumentWithMetadata = createDocumentWithMetadata();
        List<IdValue<DocumentWithMetadata>> existingDocuments = newArrayList(new IdValue<>("1", createDocumentWithMetadata()));
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(existingDocuments.get(0), new IdValue<>("2", newDocumentWithMetadata));

        when(asylumCase.read(documentField))
            .thenReturn(Optional.of(existingDocuments));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(newDocumentWithMetadata);
        when(documentsAppender.append(existingDocuments, Collections.singletonList(newDocumentWithMetadata)))
            .thenReturn(allDocuments);

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            document,
            documentField,
            tag
        );

        verify(asylumCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(existingDocuments, Collections.singletonList(newDocumentWithMetadata));
        verify(asylumCase).write(documentField, allDocuments);
    }

    private DocumentWithMetadata createDocumentWithMetadata() {
        return new DocumentWithMetadata(
            document,
            RandomStringUtils.random(20),
            "31-01-1987",
            tag
        );
    }
}