package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.em;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

public class BundleDocumentTest {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final int SORT_INDEX = 1;
    private static final Document SOURCE_DOCUMENT = mock(Document.class);

    private BundleDocument bundleDocument = new BundleDocument(NAME, DESCRIPTION, SORT_INDEX, SOURCE_DOCUMENT);


    @Test
    public void should_hold_onto_values() {

        assertEquals(SOURCE_DOCUMENT, bundleDocument.getSourceDocument());
        assertEquals(DESCRIPTION, bundleDocument.getDescription());
        assertEquals(SORT_INDEX, bundleDocument.getSortIndex());
        assertEquals(NAME, bundleDocument.getName());
    }

}
