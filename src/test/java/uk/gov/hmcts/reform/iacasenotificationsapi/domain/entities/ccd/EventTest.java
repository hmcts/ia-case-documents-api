package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class EventTest {

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "APPLY_FOR_FTPA_APPELLANT", "APPLY_FOR_FTPA_RESPONDENT" }, mode = EXCLUDE)
    void has_correct_values(Event event) {
        assertEquals(convertToCamelCase(event.name()), event.toString());
    }

    @Test
    void exclusions_have_correct_values() {
        assertEquals("applyForFTPAAppellant", APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("applyForFTPARespondent", APPLY_FOR_FTPA_RESPONDENT.toString());
    }

    private static String convertToCamelCase(String snakeCase) {
        String[] words = snakeCase.split("_");
        StringBuilder camelCase = new StringBuilder();
        camelCase.append(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            camelCase.append(words[i].substring(0, 1).toUpperCase());
            camelCase.append(words[i].substring(1).toLowerCase());
        }
        return camelCase.toString();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(120, Event.values().length);
    }
}
