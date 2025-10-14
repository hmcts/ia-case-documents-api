package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

public interface HasDocument {
    Document getDocument();

    String getDescription();
}
