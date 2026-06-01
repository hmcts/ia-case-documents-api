package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;


class BundleDocumentTest {

    private final String name = "caseApplication";

    private final String description = "Case Application Document";

    private final int sortIndex = 1;
    private final Document document = new Document("http://test.com/documents/123", "document.pdf", "application/pdf");

    private final BundleDocument bundleDocument = new BundleDocument(name, description, sortIndex, document);

    @Test
    void should_return_name() {
        assertEquals(name, bundleDocument.getName());
    }

    @Test
    void should_return_description() {
        assertEquals(description, bundleDocument.getDescription());
    }

    @Test
    void should_return_index() {
        assertEquals(sortIndex, bundleDocument.getSortIndex());
    }

    @Test
    void should_return_document() {
        assertEquals(document, bundleDocument.getSourceDocument());
    }
}
