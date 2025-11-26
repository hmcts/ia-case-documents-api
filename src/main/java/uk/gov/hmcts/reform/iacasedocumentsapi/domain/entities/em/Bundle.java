package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bundle implements CaseData {

    @Getter
    private String id;
    @Getter
    private String title;
    @Getter
    private String description;
    @Getter
    private String eligibleForStitching;
    @Getter
    private List<IdValue<BundleDocument>> documents;
    private Optional<String> stitchStatus;
    private Optional<Document> stitchedDocument;

    @Getter
    private YesOrNo hasCoversheets;
    @Getter
    private YesOrNo hasTableOfContents;
    @Getter
    private String filename;

    private Bundle() {
        // noop -- for deserializer
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        String filename
    ) {
        this(
            id,
            title,
            description,
            eligibleForStitching,
            documents,
            Optional.empty(),
            Optional.empty(),
            YesOrNo.YES,
            YesOrNo.YES,
            filename
        );
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        YesOrNo hasCoversheets,
        YesOrNo hasTableOfContents,
        String filename
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.documents = documents;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.filename = filename;
    }

    public Bundle(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        Optional<String> stitchStatus,
        Optional<Document> stitchedDocument,
        YesOrNo hasCoversheets,
        YesOrNo hasTableOfContents,
        String filename
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.documents = documents;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.filename = filename;
    }

    public Optional<String> getStitchStatus() {
        return checkIsOptional(stitchStatus);
    }

    public Optional<Document> getStitchedDocument() {
        return checkIsOptional(stitchedDocument);
    }

    //It is possible for the Optional types to be instantiated as null e.g. through Jackson
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> Optional<T> checkIsOptional(Optional<T> field) {
        if (null == field) { //NOSONAR
            return Optional.empty();
        }

        return field;
    }
}
