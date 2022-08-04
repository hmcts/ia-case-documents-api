package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

public class InterpreterLanguage {

    private String language;
    private String languageDialect;

    public InterpreterLanguage() {
        // noop -- for deserializer
    }

    public InterpreterLanguage(String languageMap, String languageDialect) {
        this.language = languageMap;
        this.languageDialect = languageDialect;
    }

    public String getLanguage() {
        return language;
    }

    public String getLanguageDialect() {
        return languageDialect;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLanguageDialect(String languageDialect) {
        this.languageDialect = languageDialect;
    }
}
