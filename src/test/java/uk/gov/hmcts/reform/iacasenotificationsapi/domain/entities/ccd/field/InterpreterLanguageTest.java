package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InterpreterLanguageTest {

    private final String language = "Some Language";
    private final String languageDialect = "Some Dialect";
    private InterpreterLanguage interpreterLanguage;

    @BeforeEach
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
