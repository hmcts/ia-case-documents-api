package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FormattedDocumentListTest {

    @Test
    public void toStringTest() {
        FormattedDocument doc1 = new FormattedDocument("some name", "some desc");
        FormattedDocument doc2 = new FormattedDocument("some other name", "some other desc");
        List<FormattedDocument> formattedDocuments = Arrays.asList(doc1, doc2);
        FormattedDocumentList formattedDocumentList = new FormattedDocumentList(formattedDocuments);

        String actual = formattedDocumentList.toString();

        assertThat(actual).isEqualTo("Document: \n"
            + "some name\n"
            + "Description: \n"
            + "some desc\n"
            + "\n"
            + "Document: \n"
            + "some other name\n"
            + "Description: \n"
            + "some other desc");
    }
}
