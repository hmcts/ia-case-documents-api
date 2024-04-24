package uk.gov.hmcts.reform.iacasenotificationsapi;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONObject;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class TestUtils {

    private TestUtils() {

    }

    public static List<IdValue<DocumentWithMetadata>> getDocumentWithMetadataList(String docId, String filename,
                                                                                  String description, DocumentTag tag) {
        return Arrays.asList(new IdValue<>(docId, getDocumentWithMetadata(docId, filename, description, tag)));
    }

    public static DocumentWithMetadata getDocumentWithMetadata(String docId, String filename,
                                                                                  String description, DocumentTag tag) {
        String documentUrl = "http://dm-store/" + docId;
        Document document = new Document(documentUrl, documentUrl + "/binary", filename);

        return new DocumentWithMetadata(document, description, LocalDate.now().toString(), tag);
    }

    public static DocumentWithDescription getDocumentWithDescription(String docId, String filename,
                                                               String description) {
        String documentUrl = "http://dm-store/" + docId;
        Document document = new Document(documentUrl, documentUrl + "/binary", filename);

        return new DocumentWithDescription(document, description);
    }

    public static boolean compareStringsAndJsonObjects(Map<String, Object> expected, Map<String, Object> actual) {

        boolean result = true;
        for (String key : expected.keySet()) {
            Object expectedObj = expected.get(key);
            Object actualObj = actual.get(key);
            String expectedValue = expectedObj instanceof String ? (String) expectedObj : ((JSONObject)expectedObj).toString();
            String actualValue = actualObj instanceof String ? (String) actualObj : ((JSONObject)actualObj).toString();
            result &= Objects.equals(actualValue, expectedValue);
        }

        return result;
    }

    public static boolean compareStrings(Map<String, String> expected, Map<String, String> actual) {

        boolean result = true;
        for (String key : expected.keySet()) {
            result &= Objects.equals(expected.get(key), actual.get(key));
        }

        return result;
    }
}
