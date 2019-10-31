package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class DocumentHandler {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public DocumentHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public void addWithMetadata(AsylumCase asylumCase, Document document, AsylumCaseDefinition documentField, DocumentTag tag) {

        final List<IdValue<DocumentWithMetadata>> existingDocuments =
            extractExistingDocuments(asylumCase, documentField);

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

        asylumCase.write(documentField, allDocuments);
    }

    public void addWithMetadataWithoutReplacingExistingDocuments(
        AsylumCase asylumCase,
        Document document,
        AsylumCaseDefinition documentField,
        DocumentTag tag
    ) {

        final List<IdValue<DocumentWithMetadata>> existingDocuments =
            extractExistingDocuments(asylumCase, documentField);

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

        asylumCase.write(documentField, allDocuments);
    }

    private List<IdValue<DocumentWithMetadata>> extractExistingDocuments(AsylumCase asylumCase, AsylumCaseDefinition documentField) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingDocuments = asylumCase
            .read(documentField);

        return maybeExistingDocuments
            .orElse(Collections.emptyList());
    }


}
