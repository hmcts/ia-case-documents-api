package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApplicationApplicantTest {

    @Test
    public void has_correct_values() {
        assertEquals("Admin Officer", ApplicationApplicant.ADMIN_OFFICER.toString());
        assertEquals("Respondent", ApplicationApplicant.RESPONDENT.toString());
    }

    @Test
    public void has_correct_applicant_types() {
        assertThat(ApplicationApplicant.from("Admin Officer").get()).isEqualByComparingTo(ApplicationApplicant.ADMIN_OFFICER);
        assertThat(ApplicationApplicant.from("Respondent").get()).isEqualByComparingTo(ApplicationApplicant.RESPONDENT);
    }

    @Test
    public void should_return_empty_when_from_unknown_value() {
        assertThat(ApplicationApplicant.from("Unknown")).isEmpty();
        assertThat(ApplicationApplicant.from("")).isEmpty();
        assertThat(ApplicationApplicant.from(null)).isEmpty();
    }

    @Test
    public void should_have_correct_getValue() {
        assertEquals("Admin Officer", ApplicationApplicant.ADMIN_OFFICER.getValue());
        assertEquals("Respondent", ApplicationApplicant.RESPONDENT.getValue());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ApplicationApplicant.values().length);
    }
}