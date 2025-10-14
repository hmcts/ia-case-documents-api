package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument;

import java.util.List;
import lombok.Value;

@Value
public class FormattedDocumentList {
    List<FormattedDocument> formattedDocuments;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        formattedDocuments.forEach(s -> {
            sb.append(s);
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        });
        return sb.toString().trim();
    }
}
