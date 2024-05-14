package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@Setter
public class ReheardHearingDocuments {

    private List<IdValue<DocumentWithMetadata>> reheardHearingDocs;

    private ReheardHearingDocuments() {
        // noop -- for deserializer
    }

    public ReheardHearingDocuments(List<IdValue<DocumentWithMetadata>> reheardHearingDocs) {
        requireNonNull(reheardHearingDocs);
        this.reheardHearingDocs = reheardHearingDocs;
    }
}
