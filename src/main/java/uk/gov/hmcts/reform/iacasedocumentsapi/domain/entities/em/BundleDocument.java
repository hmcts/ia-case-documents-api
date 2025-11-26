package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;

import lombok.Getter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Getter
public class BundleDocument implements CaseData {

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

}
