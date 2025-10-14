package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;


import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

public class BundleDocument {

    private String name;
    private String description;
    private int sortIndex;
    private Document sourceDocument;

    private BundleDocument() {
        // noop -- for deserializer
    }

    public BundleDocument(
        String name,
        String description,
        int sortIndex,
        Document sourceDocument
    ) {
        this.name = name;
        this.description = description;
        this.sortIndex = sortIndex;
        this.sourceDocument = sourceDocument;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public Document getSourceDocument() {
        return sourceDocument;
    }
}
