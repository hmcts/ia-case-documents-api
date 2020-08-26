package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.em;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;


public class BundleTest {

    private static final String ID = "some id";
    private static final String TITLE = "bundle title";
    private static final String DESCRIPTION = "bundle desc";
    private static final String ELIGIBLE_FOR_STITCHING = "yes";
    private static final List<IdValue<BundleDocument>> DOCUMENTS = Collections.emptyList();
    private static final Optional<String> STITCH_STATUS = Optional.of("DONE");
    private static final Optional<Document> STITCHED_DOCUMENT = Optional.of(mock(Document.class));

    private static final YesOrNo HAS_COVER_SHEETS = YesOrNo.NO;
    private static final YesOrNo HAS_TABLE_OF_CONTENTS = YesOrNo.YES;
    private static final String FILE_NAME = "bundle file name";

    private Bundle bundle = new Bundle(ID, TITLE, DESCRIPTION, ELIGIBLE_FOR_STITCHING, DOCUMENTS, STITCH_STATUS, STITCHED_DOCUMENT, HAS_COVER_SHEETS, HAS_TABLE_OF_CONTENTS, FILE_NAME);


    @Test
    public void should_hold_onto_values() {

        assertEquals(ID, bundle.getId());
        assertEquals(TITLE, bundle.getTitle());
        assertEquals(DESCRIPTION, bundle.getDescription());
        assertEquals(ELIGIBLE_FOR_STITCHING, bundle.getEligibleForStitching());
        assertEquals(DOCUMENTS, bundle.getDocuments());
        assertEquals(STITCH_STATUS, bundle.getStitchStatus());
        assertEquals(STITCHED_DOCUMENT, bundle.getStitchedDocument());
        assertEquals(HAS_COVER_SHEETS, bundle.getHasCoversheets());
        assertEquals(HAS_TABLE_OF_CONTENTS, bundle.getHasTableOfContents());
        assertEquals(FILE_NAME, bundle.getFilename());
    }

}
