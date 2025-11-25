package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument;

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
        String expected = new StringBuilder().append("Document: \nsome name\nDescription: \nsome desc")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Document: \nsome other name\nDescription: \nsome other desc")
            .toString();

        assertThat(actual).isEqualTo(expected);
    }
}
