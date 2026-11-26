package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;


import com.fasterxml.jackson.annotation.JsonCreator;

public class InterpreterLanguage {

    private String language;
    private String languageDialect;

    @JsonCreator
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
