package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em.BundleDocument;


class BundleDocumentTest {

    private String name = "caseApplication";

    private String description = "Case Application Document";

    private int sortIndex = 1;
    @Mock
    private Document document;

    private BundleDocument bundleDocument = new BundleDocument(name, description, sortIndex, document);

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
