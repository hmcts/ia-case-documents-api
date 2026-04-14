package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
public class DocumentHandlerTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document document;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DocumentReceiver documentReceiver;

    @InjectMocks
    private DocumentHandler documentHandler;

    private final DocumentTag tag = DocumentTag.ADDITIONAL_EVIDENCE;
    private final AsylumCaseDefinition documentField = AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

    @Test
    void should_add_document_to_empty_list() {
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
    void should_add_document_to_non_empty_list() {
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
    void should_add_document_to_non_empty_list_without_replacing_documents() {

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

    @Test
    void should_add_document_with_metadata_to_non_empty_list_without_replacing_documents() {

        DocumentWithMetadata newDocumentWithMetadata = createDocumentWithMetadata();
        List<IdValue<DocumentWithMetadata>> existingDocuments = newArrayList(new IdValue<>("1", createDocumentWithMetadata()));
        List<IdValue<DocumentWithMetadata>> allDocuments = newArrayList(existingDocuments.get(0), new IdValue<>("2", newDocumentWithMetadata));

        when(asylumCase.read(documentField))
            .thenReturn(Optional.of(existingDocuments));
        when(documentReceiver.receive(document, "", tag))
            .thenReturn(newDocumentWithMetadata);
        when(documentsAppender.append(existingDocuments, Collections.singletonList(newDocumentWithMetadata)))
            .thenReturn(allDocuments);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))
            .toLocalDateTime().format(formatter);
        documentHandler.addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            document,
            documentField,
            tag
        );
        LocalDateTime actual = LocalDateTime.parse(newDocumentWithMetadata.getDateTimeUploaded());
        assertTrue(currentDateTime.equals(actual.format(formatter))
            || currentDateTime.equals(actual.minusMinutes(1).format(formatter)));
        verify(asylumCase).read(documentField);
        verify(documentReceiver).receive(document, "", tag);
        verify(documentsAppender).append(existingDocuments, Collections.singletonList(newDocumentWithMetadata));
        verify(asylumCase).write(documentField, allDocuments);

    }

    private DocumentWithMetadata createDocumentWithMetadata() {
        return new DocumentWithMetadata(
            document,
            RandomStringUtils.secure().next(20),
            "31-01-1987",
            tag, "test"
        );
    }
}
