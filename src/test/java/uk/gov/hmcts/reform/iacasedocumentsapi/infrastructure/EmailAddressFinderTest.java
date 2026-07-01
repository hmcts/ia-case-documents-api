package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.*;

import java.util.HashMap;
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

    public static final String HC_GLASSGOW_EMAIL = "hc-glassgow@example.com";
    public static final String HC_BRADFORD_MAIL = "hc-bradford@example.com";
    public static final String HC_NORTH_SHIELDS_EMAIL = "hc-north-shields@example.com";
    public static final String HO_TAYLOR_HOUSE_EMAIL = "ho-taylorhouse@example.com";
    @Mock
    AsylumCase asylumCase;

    private Map<HearingCentre, String> hearingCentreEmailAddresses;

    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreEmailAddress = "hearingCentre@example.com";
    private final HearingCentre listCaseHearingCentre = HearingCentre.BRADFORD;
    private final String listCaseHearingCenterEmailAddress = "listCaseHearingCentre@example.com";
    private final String legalRepEmailAddress = "legalRep@example.com";

    private EmailAddressFinder emailAddressFinder;

    @BeforeEach
    void setup() {
        hearingCentreEmailAddresses = new HashMap<>();
        hearingCentreEmailAddresses.put(hearingCentre, hearingCentreEmailAddress);

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(listCaseHearingCentre));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
                .thenReturn(Optional.of(legalRepEmailAddress));

        emailAddressFinder = new EmailAddressFinder(hearingCentreEmailAddresses);
    }

    @Test
    void should_return_given_email_address_from_lookup_map() {
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
    public void should_return_correct_list_case_hearing_Centre_given_email_address_from_lookup_map() {
        // Test GLASGOW_TRIBUNAL_CENTRE -> GLASGOW mapping
        Map<HearingCentre, String> glasgowMap = new HashMap<>();
        glasgowMap.put(GLASGOW, HC_GLASSGOW_EMAIL);
        EmailAddressFinder glasgowFinder = new EmailAddressFinder(glasgowMap);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(GLASGOW_TRIBUNAL_CENTRE));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(GLASGOW_TRIBUNAL_CENTRE));
        assertEquals(HC_GLASSGOW_EMAIL, glasgowFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        // Test NOTTINGHAM -> BIRMINGHAM mapping
        Map<HearingCentre, String> birminghamMap = new HashMap<>();
        birminghamMap.put(BIRMINGHAM, HC_BRADFORD_MAIL);
        EmailAddressFinder birminghamFinder = new EmailAddressFinder(birminghamMap);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NOTTINGHAM));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NOTTINGHAM));
        assertEquals(HC_BRADFORD_MAIL, birminghamFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        // Test COVENTRY -> BIRMINGHAM mapping
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(COVENTRY));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(COVENTRY));
        assertEquals(HC_BRADFORD_MAIL, birminghamFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        // Test NEWCASTLE
        Map<HearingCentre, String> newcastleMap = new HashMap<>();
        newcastleMap.put(NEWCASTLE, HC_NORTH_SHIELDS_EMAIL);
        EmailAddressFinder newcastleFinder = new EmailAddressFinder(newcastleMap);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NEWCASTLE));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NEWCASTLE));
        assertEquals(HC_NORTH_SHIELDS_EMAIL,
                newcastleFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        // Test NORTH_SHIELDS
        Map<HearingCentre, String> northShieldsMap = new HashMap<>();
        northShieldsMap.put(NORTH_SHIELDS, HC_NORTH_SHIELDS_EMAIL);
        EmailAddressFinder northShieldsFinder = new EmailAddressFinder(northShieldsMap);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        assertEquals(HC_NORTH_SHIELDS_EMAIL,
                northShieldsFinder.getListCaseHearingCentreEmailAddress(asylumCase));

        // Test REMOTE_HEARING falls back to HEARING_CENTRE
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(NORTH_SHIELDS));
        assertEquals(HC_NORTH_SHIELDS_EMAIL,
                northShieldsFinder.getListCaseHearingCentreEmailAddress(asylumCase));

    }

    @Test
    public void should_fallback_to_hearing_centre_when_list_case_hearing_centre_is_shared() {
        Map<HearingCentre, String> taylorHouseMap = new HashMap<>();
        taylorHouseMap.put(TAYLOR_HOUSE, HO_TAYLOR_HOUSE_EMAIL);
        EmailAddressFinder taylorHouseFinder = new EmailAddressFinder(taylorHouseMap);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HENDON));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(TAYLOR_HOUSE));
        assertEquals(HO_TAYLOR_HOUSE_EMAIL,
                taylorHouseFinder.getListCaseHearingCentreEmailAddress(asylumCase));
    }

}
