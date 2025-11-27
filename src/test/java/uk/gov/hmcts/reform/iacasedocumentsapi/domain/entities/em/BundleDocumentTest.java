package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

public class BundleDocumentTest {
    private final String name = "caseApplication";

    private final String description = "Case Application Document";

    private final int sortIndex = 1;
    @Mock
    private Document document;

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

    @Test
    public void should_hold_onto_values() {

        assertEquals(document, bundleDocument.getSourceDocument());
        assertEquals(description, bundleDocument.getDescription());
        assertEquals(sortIndex, bundleDocument.getSortIndex());
        assertEquals(name, bundleDocument.getName());
    }
}
