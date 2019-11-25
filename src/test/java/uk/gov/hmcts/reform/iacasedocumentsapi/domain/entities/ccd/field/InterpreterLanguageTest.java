package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InterpreterLanguageTest {

    private final String language = "Some Language";
    private final String languageDialect = "Some Dialect";
    private InterpreterLanguage interpreterLanguage;

    @Before
    public void setUp() {
        interpreterLanguage = new InterpreterLanguage();
        interpreterLanguage.setLanguage(language);
        interpreterLanguage.setLanguageDialect(languageDialect);
    }

    @Test
    public void should_hold_onto_values() {

        Assert.assertEquals(language, interpreterLanguage.getLanguage());
        Assert.assertEquals(languageDialect, interpreterLanguage.getLanguageDialect());
    }
}
