package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum BailHearingLocation {

    MANCHESTER("manchester", "Manchester"),
    BIRMINGHAM("birmingham", "Birmingham"),
    BRADFORD("bradford", "Bradford"),
    COVENTRY("coventry", "Coventry Magistrates Court"),
    GLASGOW_TRIBUNAL_CENTRE("glasgowTribunalsCentre", "Glasgow"),
    HARMONDSWORTH("harmondsworth", "Harmondsworth"),
    HATTON_CROSS("hattonCross", "Hatton Cross"),
    HENDON("hendon", "Hendon"),
    NEWCASTLE("newcastle", "Newcastle Civil & Family Courts and Tribunals Centre"),
    NEWPORT("newport", "Newport"),
    TAYLOR_HOUSE("taylorHouse", "Taylor House"),
    YARLS_WOOD("yarlsWood", "Yarl's Wood"),
    BRADFORD_KEIGHLEY("bradfordKeighley", "Bradford & Keighley"),
    MCC_MINSHULL("mccMinshull", "MCC Minshull st"),
    MCC_CROWN_SQUARE("mccCrownSquare", "MCC Crown Square"),
    NOTTINGHAM("nottingham", "Nottingham Justice Centre"),
    MANCHESTER_MAGS("manchesterMags", "Manchester Mags"),
    NTH_TYNE_MAGS("nthTyneMags", "NTH Tyne Mags"),
    LEEDS_MAGS("leedsMags", "Leeds Mags"),
    BELFAST("belfast", "Belfast"),
    ALLOA_SHERRIF("alloaSherrif", "Alloa Sherrif Court"),
    REMOTE_HEARING("remoteHearing", "Remote"),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing", "Decision without hearing");

    @JsonValue
    private final String value;

    private String description;

    BailHearingLocation(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<BailHearingLocation> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
