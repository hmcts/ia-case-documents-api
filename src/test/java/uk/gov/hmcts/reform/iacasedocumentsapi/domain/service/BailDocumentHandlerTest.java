package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
public class BailDocumentHandlerTest {
    @Mock private DocumentReceiver documentReceiver;

    @Mock private DocumentsAppender documentsAppender;

    @Mock private BailCase bailCase;

    @Mock private Document document;

    private final DocumentTag tag = DocumentTag.ADDITIONAL_EVIDENCE;

    private final BailCaseFieldDefinition documentField = BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;

    private BailDocumentHandler bailDocumentHandler;

    @BeforeEach
    public void setUp() {
        bailDocumentHandler = new BailDocumentHandler(documentReceiver, documentsAppender);
    }

    @Test
    public void should_add_document_to_empty_list() {
        DocumentWithMetadata documentWithMetadata = createDocumentWithMetaData();
        List<IdValue<DocumentWithMetadata>> documents = newArrayList();
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(new IdValue<>("1", documentWithMetadata));

        when(bailCase.read(documentField))
            .thenReturn(Optional.of(documents));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(documentWithMetadata);
        when(documentsAppender.append(documents, Collections.singletonList(documentWithMetadata), tag))
            .thenReturn(allDocuments);

        bailDocumentHandler.addWithMetadata(
            bailCase,
            document,
            documentField,
            tag
        );

        verify(bailCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(documents, Collections.singletonList(documentWithMetadata), tag);
        verify(bailCase).write(documentField, allDocuments);
    }

    @Test
    public void should_add_document_to_non_empty_list() {
        DocumentWithMetadata documentWithMetadata = createDocumentWithMetaData();
        List<IdValue<DocumentWithMetadata>> documents = newArrayList(new IdValue<>("1", createDocumentWithMetaData()));
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(documents.get(0), new IdValue<>("2", documentWithMetadata));

        when(bailCase.read(documentField))
            .thenReturn(Optional.of(documents));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(documentWithMetadata);
        when(documentsAppender.append(documents, Collections.singletonList(documentWithMetadata), tag))
            .thenReturn(allDocuments);

        bailDocumentHandler.addWithMetadata(
            bailCase,
            document,
            documentField,
            tag
        );

        verify(bailCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(documents, Collections.singletonList(documentWithMetadata), tag);
        verify(bailCase).write(documentField, allDocuments);
    }

    @Test
    public void should_add_document_to_non_empty_list_without_replacing_documents() {

        DocumentWithMetadata newDocumentWithMetadata = createDocumentWithMetaData();
        List<IdValue<DocumentWithMetadata>> existingDocuments = newArrayList(new IdValue<>("1", createDocumentWithMetaData()));
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(existingDocuments.get(0), new IdValue<>("2", newDocumentWithMetadata));

        when(bailCase.read(documentField))
            .thenReturn(Optional.of(existingDocuments));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(newDocumentWithMetadata);
        when(documentsAppender.append(existingDocuments, Collections.singletonList(newDocumentWithMetadata)))
            .thenReturn(allDocuments);

        bailDocumentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            bailCase,
            document,
            documentField,
            tag
        );

        verify(bailCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(existingDocuments, Collections.singletonList(newDocumentWithMetadata));
        verify(bailCase).write(documentField, allDocuments);
    }

    private DocumentWithMetadata createDocumentWithMetaData() {
        return new DocumentWithMetadata(
            document,
            RandomStringUtils.random(20),
            "31-01-1987",
            tag, "test"
        );
    }


}
