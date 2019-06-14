package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("unchecked")
public class BundleTest {

    private String id = someRandomString();
    private String title = someRandomString();
    private String description = someRandomString();
    private String eligibleForStitching = someRandomString();
    private List<IdValue<BundleDocument>> documents = singletonList(mock(IdValue.class));
    private Optional<String> stitchStatus = Optional.of(someRandomString());
    private Optional<Document> stitchedDocument = Optional.of(mock(Document.class));
    private YesOrNo hasCoversheets = YesOrNo.YES;

    private YesOrNo hasTableOfContents = YesOrNo.NO;
    private String filename = someRandomString();
    private Bundle bundle;

    @Before
    public void setUp() {
        bundle = new Bundle(
                id,
                title,
                description,
                eligibleForStitching,
                documents,
                stitchStatus,
                stitchedDocument,
                hasCoversheets,
                hasTableOfContents,
                filename
        );
    }

    @Test
    public void buildsObjectCorrectly() {
        assertThat(bundle.getId()).isEqualTo(id);
        assertThat(bundle.getTitle()).isEqualTo(title);
        assertThat(bundle.getDescription()).isEqualTo(description);
        assertThat(bundle.getEligibleForStitching()).isEqualTo(eligibleForStitching);
        assertThat(bundle.getDocuments()).isEqualTo(documents);
        assertThat(bundle.getStitchStatus()).isEqualTo(stitchStatus);
        assertThat(bundle.getStitchedDocument()).isEqualTo(stitchedDocument);
        assertThat(bundle.getHasCoversheets()).isEqualTo(hasCoversheets);
        assertThat(bundle.getHasTableOfContents()).isEqualTo(hasTableOfContents);
        assertThat(bundle.getFilename()).isEqualTo(filename);
    }


    private String someRandomString() {
        return randomAlphabetic(8);
    }
}