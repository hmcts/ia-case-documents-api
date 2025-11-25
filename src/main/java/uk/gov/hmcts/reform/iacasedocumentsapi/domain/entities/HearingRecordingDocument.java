package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import lombok.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Value
public class HearingRecordingDocument implements HasDocument {
    Document document;
    String description;
}
