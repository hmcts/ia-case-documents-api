package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
class DetentionEmailServiceTest {

    private final String ctscEmail = "ctsc@hmcts.net";
    @Mock
    private DetEmailService detEmailService;

    @Mock
    private AsylumCase asylumCase;

    private DetentionEmailService detentionEmailService;

    @BeforeEach
    void setUp() {
        detentionEmailService = new DetentionEmailService(detEmailService, ctscEmail);
    }

    @Test
    void should_return_det_email_for_irc() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn("irc@example.com");

        String email = detentionEmailService.getDetentionEmailAddress(asylumCase);

        assertEquals("irc@example.com", email);
        verify(detEmailService).getDetEmailAddress(asylumCase);
    }

    @Test
    void should_return_prison_email_for_prison_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        String email = detentionEmailService.getDetentionEmailAddress(asylumCase);

        assertEquals(ctscEmail, email);
    }

    @Test
    void should_throw_if_detention_facility_not_present() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detentionEmailService.getDetentionEmailAddress(asylumCase));

        assertEquals("Detention facility is not present", ex.getMessage());
    }

    @Test
    void should_throw_if_detention_facility_not_valid() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detentionEmailService.getDetentionEmailAddress(asylumCase));

        assertEquals("Detention facility is not valid", ex.getMessage());
    }
}
