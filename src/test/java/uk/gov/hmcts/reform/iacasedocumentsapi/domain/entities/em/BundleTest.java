package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;


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

    private Bundle bundle;

    @BeforeEach
    public void setUp() {
        bundle = new Bundle(
            ID, TITLE, DESCRIPTION, ELIGIBLE_FOR_STITCHING, DOCUMENTS, STITCH_STATUS, STITCHED_DOCUMENT,
            HAS_COVER_SHEETS, HAS_TABLE_OF_CONTENTS, FILE_NAME
        );
    }

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

    @Test
    public void should_hold_onto_values_for_less_fields() {

        bundle =
            new Bundle(ID, TITLE, DESCRIPTION, ELIGIBLE_FOR_STITCHING, DOCUMENTS, FILE_NAME);

        assertEquals(ID, bundle.getId());
        assertEquals(TITLE, bundle.getTitle());
        assertEquals(DESCRIPTION, bundle.getDescription());
        assertEquals(ELIGIBLE_FOR_STITCHING, bundle.getEligibleForStitching());
        assertEquals(DOCUMENTS, bundle.getDocuments());
        assertEquals(Optional.empty(), bundle.getStitchStatus());
        assertEquals(Optional.empty(), bundle.getStitchedDocument());
        assertEquals(YesOrNo.YES, bundle.getHasCoversheets());
        assertEquals(YesOrNo.YES, bundle.getHasTableOfContents());
        assertEquals(FILE_NAME, bundle.getFilename());
    }

    @Test
    public void shouldConvertNullValuesToOptionalEmpty() {
        bundle = new Bundle(
            ID,
            TITLE,
            DESCRIPTION,
            ELIGIBLE_FOR_STITCHING,
            DOCUMENTS,
            null,
            null,
            HAS_COVER_SHEETS,
            HAS_TABLE_OF_CONTENTS,
            FILE_NAME
        );

        assertEquals(bundle.getStitchStatus(), Optional.empty());
        assertEquals(bundle.getStitchedDocument(), Optional.empty());

    }

    @Test
    public void shouldConvertNullValuesToOptionalEmptyCoverSheetContentsConstructor() {
        bundle = new Bundle(
            ID,
            TITLE,
            DESCRIPTION,
            ELIGIBLE_FOR_STITCHING,
            DOCUMENTS,
            HAS_COVER_SHEETS,
            HAS_TABLE_OF_CONTENTS,
            FILE_NAME
        );

        assertEquals(bundle.getStitchStatus(), Optional.empty());
        assertEquals(bundle.getStitchedDocument(), Optional.empty());

    }

    @Test
    public void shouldConvertNullValuesToOptionalEmptyWhenUsingEmptyConstructor() throws Exception {

        bundle = findPrivateNoArgsConstructor(Bundle.class).newInstance();

        assertEquals(bundle.getStitchStatus(), Optional.empty());
        assertEquals(bundle.getStitchedDocument(), Optional.empty());

    }

    private <T> Constructor<T> findPrivateNoArgsConstructor(Class<T> clazz) {

        return (Constructor<T>)
            Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor ->
                            Modifier.isPrivate(constructor.getModifiers()) && constructor.getParameterCount() == 0)
                .peek(constructor -> constructor.setAccessible(true))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not find private no-args constructor"));
    }


    private String someRandomString() {
        return secure().nextAlphabetic(8);
    }

}
