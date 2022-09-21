package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class TsvStringProviderTest {

    @Autowired private TsvStringProvider tsvStringProvider;

    @Test
    public void should_load_strings_from_resources_and_return() {

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
            Optional.of("Removing the appellant from the UK would breach the UK's obligation under the Refugee Convention"),
            tsvStringProvider.get("appealGrounds", "protectionRefugeeConvention")
        );

        assertEquals(
            Optional.of("Removing the appellant from the UK would breach the UK's obligation in relation to persons eligible for a grant of humanitarian protection"),
            tsvStringProvider.get("appealGrounds", "protectionHumanitarianProtection")
        );

        assertEquals(
            Optional.of("Removing the appellant from the UK would be unlawful under section 6 of the Human Rights Act 1998"),
            tsvStringProvider.get("appealGrounds", "protectionHumanRights")
        );

        assertEquals(
            Optional.of("The decision is unlawful under section 6 of the Human Rights Act 1998"),
            tsvStringProvider.get("appealGrounds", "humanRightsRefusal")
        );

        assertEquals(
            Optional.of("Revocation of the appellant's protection status breaches the United Kingdom's obligations under the Refugee Convention"),
            tsvStringProvider.get("appealGrounds", "revocationRefugeeConvention")
        );

        assertEquals(
            Optional.of("Revocation of the appellant's protection status breaches the United Kingdom's obligations in relation to persons eligible for humanitarian protection"),
            tsvStringProvider.get("appealGrounds", "revocationHumanitarianProtection")
        );

        assertEquals(
            Optional.of("The refusal of a protection claim"),
            tsvStringProvider.get("appealType", "protection")
        );

        assertEquals(
            Optional.of("The revocation of a protection status"),
            tsvStringProvider.get("appealType", "revocationOfProtection")
        );

        assertEquals(
            Optional.of("IAC Birmingham, Birmingham Justice Centre, 33 Bull Street, Birmingham, B4 6DS"),
            tsvStringProvider.get("hearingCentreAddress", "birmingham")
        );

        assertEquals(
            Optional.of("Belfast Laganside Court, Oxford Street, Belfast, BT1 3LL"),
            tsvStringProvider.get("hearingCentreAddress", "belfast")
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
            Optional.of("IAC Glasgow, 1st Floor, The Glasgow Tribunals Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT"),
            tsvStringProvider.get("hearingCentreAddress", "glasgowTribunalsCentre")
        );

        assertEquals(
            Optional.of("IAC Hatton Cross, York House, 2-3 Dukes Green Avenue, Feltham, Middlesex, TW14 0LS"),
            tsvStringProvider.get("hearingCentreAddress", "hattonCross")
        );

        assertEquals(
            Optional.of("IAC Manchester, 1st Floor Piccadilly Exchange, 2 Piccadilly Plaza, Mosley Street, Manchester, M1 4AH"),
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
            Optional.of("Newcastle Civil & Family Courts and Tribunals Centre, Barras Bridge, Newcastle upon Tyne, NE1 8QF"),
            tsvStringProvider.get("hearingCentreAddress", "newcastle")
        );

        assertEquals(
            Optional.of("IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU"),
            tsvStringProvider.get("hearingCentreAddress", "taylorHouse")
        );

        assertEquals(
            Optional.of("Afghanistan"),
            tsvStringProvider.get("isoCountries", "AF")
        );

        assertEquals(
            Optional.of("Aruba"),
            tsvStringProvider.get("isoCountries", "AW")
        );

        assertEquals(
            Optional.of("Zimbabwe"),
            tsvStringProvider.get("isoCountries", "ZW")
        );


        assertEquals(
            Optional.of("Stateless"),
            tsvStringProvider.get("isoCountries", "ZZ")
        );
    }

    @Test
    public void should_return_empty_optional_if_string_not_found() {

        assertEquals(
            Optional.empty(),
            tsvStringProvider.get("not", "exists")
        );
    }
}
