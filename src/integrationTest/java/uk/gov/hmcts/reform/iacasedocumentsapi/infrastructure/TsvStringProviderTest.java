package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.Application;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class TsvStringProviderTest {

    @Autowired private TsvStringProvider tsvStringProvider;

    @Test
    public void should_load_strings_from_resources_and_return() {

        assertEquals(
            Optional.of("Bradford"),
            tsvStringProvider.get("hearingCentreName", "bradford")
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
            Optional.of("Birmingham"),
            tsvStringProvider.get("hearingCentreName", "birmingham")
        );

        assertEquals(
            Optional.of("Hatton Cross"),
            tsvStringProvider.get("hearingCentreName", "hattonCross")
        );

        assertEquals(
            Optional.of("Glasgow"),
            tsvStringProvider.get("hearingCentreName", "glasgow")
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
            Optional.of("IAC Manchester, 1st Floor Piccadilly Exchange, 2 Piccadilly Plaza, Mosley Street, Manchester, M1 4AH"),
            tsvStringProvider.get("hearingCentreAddress", "manchester")
        );

        assertEquals(
            Optional.of("IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU"),
            tsvStringProvider.get("hearingCentreAddress", "taylorHouse")
        );

        assertEquals(
            Optional.of("IAC North Shields, Kings Court, Royal Quays, Earl Grey Way, North Shields, NE29 6AR"),
            tsvStringProvider.get("hearingCentreAddress", "northShields")
        );

        assertEquals(
            Optional.of("IAC Birmingham, Birmingham Justice Centre, 33 Bull Street, Birmingham, B4 6DS"),
            tsvStringProvider.get("hearingCentreAddress", "birmingham")
        );

        assertEquals(
            Optional.of("IAC Hatton Cross, York House, 2-3 Dukes Green Avenue, Feltham, Middlesex, TW14 0LS"),
            tsvStringProvider.get("hearingCentreAddress", "hattonCross")
        );

        assertEquals(
            Optional.of("IAC Glasgow, 4th Floor, Eagle Building, 215 Bothwell Street, Glasgow, G2 7EZ"),
            tsvStringProvider.get("hearingCentreAddress", "glasgow")
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
    }

    @Test
    public void should_return_empty_optional_if_string_not_found() {

        assertEquals(
            Optional.empty(),
            tsvStringProvider.get("not", "exists")
        );
    }
}
