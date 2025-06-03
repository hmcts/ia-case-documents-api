package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import java.util.List;
import lombok.Getter;


@Getter
public class LegRepAddressUk {

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String townCity;

    private String county;

    private String postCode;

    private String country;

    private List<String> dxAddress;

    private LegRepAddressUk() {
        // noop -- for deserializer
    }

    public LegRepAddressUk(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String townCity,
        String county,
        String postCode,
        String country,
        List<String> dxAddress
    ) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.townCity = townCity;
        this.county = county;
        this.postCode = postCode;
        this.country = country;
        this.dxAddress = dxAddress;
    }

}
