package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import lombok.Value;

@Value
public class FormattedDocument {
    String filename;
    String description;

    @Override
    public String toString() {
        return "Document: \n" + filename + '\n'
            + "Description: \n" + description;
    }
}
