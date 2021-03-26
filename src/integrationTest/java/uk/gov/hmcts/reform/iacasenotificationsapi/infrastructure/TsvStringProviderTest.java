package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
class TsvStringProviderTest {

    @Autowired
    private TsvStringProvider tsvStringProvider;

    @Test
    void should_load_strings_from_resources_and_return() {

        assertEquals(
            Optional.of("Birmingham"),
            tsvStringProvider.get("hearingCentre", "birmingham")
        );

        assertEquals(
            Optional.of("Bradford"),
            tsvStringProvider.get("hearingCentre", "bradford")
        );

        assertEquals(
            Optional.of("Coventry Magistrates Court"),
            tsvStringProvider.get("hearingCentre", "coventry")
        );

        assertEquals(
            Optional.of("Glasgow (Eagle Building)"),
            tsvStringProvider.get("hearingCentre", "glasgow")
        );

        assertEquals(
            Optional.of("Glasgow Tribunals Centre"),
            tsvStringProvider.get("hearingCentre", "glasgowTribunalsCentre")
        );

        assertEquals(
            Optional.of("Hatton Cross"),
            tsvStringProvider.get("hearingCentre", "hattonCross")
        );

        assertEquals(
            Optional.of("Manchester"),
            tsvStringProvider.get("hearingCentre", "manchester")
        );

        assertEquals(
            Optional.of("Newcastle Civil & Family Courts and Tribunals Centre"),
            tsvStringProvider.get("hearingCentre", "newcastle")
        );

        assertEquals(
            Optional.of("Newport"),
            tsvStringProvider.get("hearingCentre", "newport")
        );

        assertEquals(
            Optional.of("North Shields"),
            tsvStringProvider.get("hearingCentre", "northShields")
        );

        assertEquals(
            Optional.of("Nottingham Justice Centre"),
            tsvStringProvider.get("hearingCentre", "nottingham")
        );

        assertEquals(
            Optional.of("Taylor House"),
            tsvStringProvider.get("hearingCentre", "taylorHouse")
        );

        assertEquals(
            Optional.of("Remote hearing"),
            tsvStringProvider.get("hearingCentre", "remoteHearing")
        );

        assertEquals(
            Optional.of("Birmingham"),
            tsvStringProvider.get("hearingCentreName", "birmingham")
        );

        assertEquals(
            Optional.of("Bradford"),
            tsvStringProvider.get("hearingCentreName", "bradford")
        );

        assertEquals(
            Optional.of("Coventry Magistrates Court"),
            tsvStringProvider.get("hearingCentreName", "coventry")
        );

        assertEquals(
            Optional.of("Glasgow (Eagle Building)"),
            tsvStringProvider.get("hearingCentreName", "glasgow")
        );

        assertEquals(
            Optional.of("Glasgow Tribunals Centre"),
            tsvStringProvider.get("hearingCentreName", "glasgowTribunalsCentre")
        );

        assertEquals(
            Optional.of("Hatton Cross"),
            tsvStringProvider.get("hearingCentreName", "hattonCross")
        );

        assertEquals(
            Optional.of("Manchester"),
            tsvStringProvider.get("hearingCentreName", "manchester")
        );

        assertEquals(
            Optional.of("Newcastle Civil & Family Courts and Tribunals Centre"),
            tsvStringProvider.get("hearingCentreName", "newcastle")
        );

        assertEquals(
            Optional.of("Newport"),
            tsvStringProvider.get("hearingCentreName", "newport")
        );

        assertEquals(
            Optional.of("North Shields"),
            tsvStringProvider.get("hearingCentreName", "northShields")
        );

        assertEquals(
            Optional.of("Nottingham Justice Centre"),
            tsvStringProvider.get("hearingCentreName", "nottingham")
        );

        assertEquals(
            Optional.of("Taylor House"),
            tsvStringProvider.get("hearingCentreName", "taylorHouse")
        );

        assertEquals(
            Optional.of("Remote hearing"),
            tsvStringProvider.get("hearingCentreName", "remoteHearing")
        );

        assertEquals(
            Optional.of("IAC Birmingham, Birmingham Justice Centre, 33 Bull Street, Birmingham, B4 6DS"),
            tsvStringProvider.get("hearingCentreAddress", "birmingham")
        );

        assertEquals(
            Optional.of("IAC Bradford, Phoenix House, Rushton Avenue, Thornbury, Bradford, BD3 7BH"),
            tsvStringProvider.get("hearingCentreAddress", "bradford")
        );

        assertEquals(
            Optional.of("Coventry Magistrates Court, Little Park Street, Coventry, West Midlands, CV1 2SQ"),
            tsvStringProvider.get("hearingCentreAddress", "coventry")
        );

        assertEquals(
            Optional.of("IAC Glasgow, 4th Floor, Eagle Building, 215 Bothwell Street, Glasgow, G2 7EZ"),
            tsvStringProvider.get("hearingCentreAddress", "glasgow")
        );

        assertEquals(
            Optional
                .of("IAC Glasgow, 1st Floor, The Glasgow Tribunals Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT"),
            tsvStringProvider.get("hearingCentreAddress", "glasgowTribunalsCentre")
        );

        assertEquals(
            Optional.of("IAC Hatton Cross, York House, 2-3 Dukes Green Avenue, Feltham, Middlesex, TW14 0LS"),
            tsvStringProvider.get("hearingCentreAddress", "hattonCross")
        );

        assertEquals(
            Optional
                .of("IAC Manchester, 1st Floor Piccadilly Exchange, 2 Piccadilly Plaza, Mosley Street, Manchester, M1 4AH"),
            tsvStringProvider.get("hearingCentreAddress", "manchester")
        );

        assertEquals(
            Optional.of("IAC Newport, Columbus House, Langstone Business Park, Chepstow Road, Newport, NP18 2LX"),
            tsvStringProvider.get("hearingCentreAddress", "newport")
        );

        assertEquals(
            Optional.of("IAC North Shields, Kings Court, Royal Quays, Earl Grey Way, North Shields, NE29 6AR"),
            tsvStringProvider.get("hearingCentreAddress", "northShields")
        );

        assertEquals(
            Optional.of("Nottingham Justice Centre, Carrington Street, Nottingham, NG2 1EE"),
            tsvStringProvider.get("hearingCentreAddress", "nottingham")
        );

        assertEquals(
            Optional
                .of("Newcastle Civil & Family Courts and Tribunals Centre, Barras Bridge, Newcastle upon Tyne, NE1 8QF"),
            tsvStringProvider.get("hearingCentreAddress", "newcastle")
        );

        assertEquals(
            Optional.of("IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU"),
            tsvStringProvider.get("hearingCentreAddress", "taylorHouse")
        );
        assertEquals(
            Optional.of("Remote hearing"),
            tsvStringProvider.get("hearingCentreAddress", "remoteHearing")
        );
    }

    @Test
    void should_return_empty_optional_if_string_not_found() {

        assertEquals(
            Optional.empty(),
            tsvStringProvider.get("not", "exists")
        );
    }
}
