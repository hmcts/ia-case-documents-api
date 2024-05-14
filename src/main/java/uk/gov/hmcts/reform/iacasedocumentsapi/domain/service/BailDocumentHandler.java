package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class BailDocumentHandler {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public BailDocumentHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public void addWithMetadata(BailCase bailCase, Document document, BailCaseFieldDefinition documentField, DocumentTag tag) {

        final List<IdValue<DocumentWithMetadata>> existingDocuments =
            extractExistingDocuments(bailCase, documentField);

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                document,
                "",
                tag
            );

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(
                existingDocuments,
                Collections.singletonList(documentWithMetadata),
                tag
            );

        bailCase.write(documentField, allDocuments);
    }

    public void appendWithMetadata(
        BailCase bailCase,
        Document document,
        BailCaseFieldDefinition documentField,
        DocumentTag tag
    ) {

        final List<IdValue<DocumentWithMetadata>> existingDocuments =
            extractExistingDocuments(bailCase, documentField);

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                document,
                "",
                tag
            );

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(
                existingDocuments,
                Collections.singletonList(documentWithMetadata)
            );

        bailCase.write(documentField, allDocuments);
    }

    private List<IdValue<DocumentWithMetadata>> extractExistingDocuments(BailCase bailCase, BailCaseFieldDefinition documentField) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingDocuments = bailCase
            .read(documentField);

        return maybeExistingDocuments
            .orElse(Collections.emptyList());
    }

    public void addDocumentWithoutMetadata(BailCase bailCase, Document document, BailCaseFieldDefinition documentField) {
        bailCase.write(documentField, document);
    }


}
