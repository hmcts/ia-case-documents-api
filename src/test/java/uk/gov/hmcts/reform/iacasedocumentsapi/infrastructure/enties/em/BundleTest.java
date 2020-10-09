package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
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
        assertEquals(bundle.getId(), id);
        assertEquals(bundle.getTitle(), title);
        assertEquals(bundle.getDescription(), description);
        assertEquals(bundle.getEligibleForStitching(), eligibleForStitching);
        assertEquals(bundle.getDocuments(), documents);
        assertEquals(bundle.getStitchStatus(), stitchStatus);
        assertEquals(bundle.getStitchedDocument(), stitchedDocument);
        assertEquals(bundle.getHasCoversheets(), hasCoversheets);
        assertEquals(bundle.getHasTableOfContents(), hasTableOfContents);
        assertEquals(bundle.getFilename(), filename);
    }

    @Test
    public void shouldConvertNullValuesToOptionalEmpty() {
        bundle = new Bundle(
            id,
            title,
            description,
            eligibleForStitching,
            documents,
            null,
            null,
            hasCoversheets,
            hasTableOfContents,
            filename
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
        return randomAlphabetic(8);
    }

}
