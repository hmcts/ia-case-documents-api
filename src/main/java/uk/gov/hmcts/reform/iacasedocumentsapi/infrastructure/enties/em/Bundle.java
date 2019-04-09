package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class Bundle implements CaseData {

    private String id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private List<IdValue<BundleDocument>> documents;
    private Optional<String> stitchStatus;
    private Optional<Document> stitchedDocument;

    private Bundle() {
        // noop -- for deserializer
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents
    ) {
        this(
            id,
            title,
            description,
            eligibleForStitching,
            documents,
            Optional.empty(),
            Optional.empty()
        );
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        Optional<String> stitchStatus,
        Optional<Document> stitchedDocument
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.documents = documents;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    public List<IdValue<BundleDocument>> getDocuments() {
        return documents;
    }

    public Optional<String> getStitchStatus() {
        return stitchStatus;
    }

    public Optional<Document> getStitchedDocument() {
        return stitchedDocument;
    }
}
