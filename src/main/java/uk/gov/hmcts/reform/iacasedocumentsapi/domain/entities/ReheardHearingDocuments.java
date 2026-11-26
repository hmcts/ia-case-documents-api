package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@Setter
public class ReheardHearingDocuments {

    private List<IdValue<DocumentWithMetadata>> reheardHearingDocs;

    @JsonCreator
    public ReheardHearingDocuments() {
        // noop -- for deserializer
    }

    public ReheardHearingDocuments(List<IdValue<DocumentWithMetadata>> reheardHearingDocs) {
        requireNonNull(reheardHearingDocs);
        this.reheardHearingDocs = reheardHearingDocs;
    }
}
