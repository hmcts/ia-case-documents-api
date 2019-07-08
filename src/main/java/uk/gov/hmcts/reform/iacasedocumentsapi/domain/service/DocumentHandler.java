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
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLegalRepresentativeDocuments = asylumCase
            .read(documentField);

        final List<IdValue<DocumentWithMetadata>> documents =
            maybeLegalRepresentativeDocuments
                .orElse(Collections.emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                document,
                "",
                tag
            );

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(
                documents,
                Collections.singletonList(documentWithMetadata),
                tag
            );

        asylumCase.write(documentField, allDocuments);
    }
}
