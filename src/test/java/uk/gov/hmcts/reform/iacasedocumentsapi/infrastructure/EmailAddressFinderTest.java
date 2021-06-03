package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailAddressFinderTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Map<HearingCentre, String> hearingCentreEmailAddresses;

    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreEmailAddress = "hearingCentre@example.com";
    private final HearingCentre listCaseHearingCentre = HearingCentre.BRADFORD;
    private final String listCaseHearingCenterEmailAddress = "listCaseHearingCentre@example.com";
    private final String legalRepEmailAddress = "legalRep@example.com";

    private EmailAddressFinder emailAddressFinder;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(listCaseHearingCentre));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
                .thenReturn(Optional.of(legalRepEmailAddress));
        when(hearingCentreEmailAddresses.get(hearingCentre)).thenReturn(hearingCentreEmailAddress);

        emailAddressFinder = new EmailAddressFinder(hearingCentreEmailAddresses);
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertEquals(hearingCentreEmailAddress, emailAddressFinder.getHearingCentreEmailAddress(asylumCase));
    }


    @Test
    public void should_throw_exception_on_email_address_when_hearing_centre_is_empty() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("hearingCentre is not present");
    }

    @Test
    public void should_return_given_legal_rep_email_address_from_lookup_map() {
        assertEquals(legalRepEmailAddress, emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_on_legal_rep_email_address_when_is_empty() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAddressFinder.getLegalRepEmailAddress(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_return_correct_list_case_hearing_Centre_given_email_address_from_lookup_map() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(GLASGOW_TRIBUNAL_CENTRE));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(GLASGOW_TRIBUNAL_CENTRE));
        when(hearingCentreEmailAddresses.get(GLASGOW)).thenReturn("hc-glassgow@example.com");
        assertEquals("hc-glassgow@example.com", emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NOTTINGHAM));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NOTTINGHAM));
        when(hearingCentreEmailAddresses.get(BIRMINGHAM)).thenReturn("hc-bradford@example.com");
        assertEquals("hc-bradford@example.com", emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(COVENTRY));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(COVENTRY));
        when(hearingCentreEmailAddresses.get(BIRMINGHAM)).thenReturn("hc-bradford@example.com");
        assertEquals("hc-bradford@example.com", emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NEWCASTLE));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NEWCASTLE));
        when(hearingCentreEmailAddresses.get(NEWCASTLE)).thenReturn("hc-north-shields@example.com");
        assertEquals("hc-north-shields@example.com",
                emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        when(hearingCentreEmailAddresses.get(NORTH_SHIELDS)).thenReturn("hc-north-shields@example.com");
        assertEquals("hc-north-shields@example.com",
                emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        when(hearingCentreEmailAddresses.get(NORTH_SHIELDS)).thenReturn("hc-north-shields@example.com");
        assertEquals("hc-north-shields@example.com",
                emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

    }

}
