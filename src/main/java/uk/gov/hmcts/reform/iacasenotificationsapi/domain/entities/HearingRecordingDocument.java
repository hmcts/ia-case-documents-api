package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import lombok.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

@Value
public class HearingRecordingDocument implements HasDocument {
    Document document;
    String description;
}
