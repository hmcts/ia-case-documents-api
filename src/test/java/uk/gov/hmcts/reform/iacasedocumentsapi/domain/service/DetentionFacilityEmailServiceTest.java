package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
class DetentionFacilityEmailServiceTest {

    @Mock
    private DetEmailService detEmailService;

    @Mock
    private PrisonEmailMappingService prisonEmailMappingService;

    @Mock
    private AsylumCase asylumCase;

    @InjectMocks
    private DetentionFacilityEmailService detentionFacilityEmailService;

    @Test
    void should_return_det_email_for_irc() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn("irc@example.com");

        String email = detentionFacilityEmailService.getDetentionEmailAddress(asylumCase);

        assertEquals("irc@example.com", email);
        verify(detEmailService).getDetEmailAddress(asylumCase);
        verifyNoInteractions(prisonEmailMappingService);
    }

    @Test
    void should_return_prison_email_for_prison_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("HMP Test"));
        when(prisonEmailMappingService.getPrisonEmail("HMP Test")).thenReturn(Optional.of("prison@example.com"));

        String email = detentionFacilityEmailService.getDetentionEmailAddress(asylumCase);

        assertEquals("prison@example.com", email);
    }

    @Test
    void should_throw_if_prison_email_not_found() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("HMP Unknown"));
        when(prisonEmailMappingService.getPrisonEmail("HMP Unknown")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));

        assertEquals("Prison email address not found for Prison: HMP Unknown", ex.getMessage());
    }

    @Test
    void should_throw_if_prison_name_not_present() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));

        assertEquals("Prison name is not present", ex.getMessage());
    }

    @Test
    void should_throw_if_detention_facility_not_present() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));

        assertEquals("Detention facility is not present", ex.getMessage());
    }
}
