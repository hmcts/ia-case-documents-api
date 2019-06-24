package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType;
import static org.apache.commons.lang.RandomStringUtils.random;

public class DocmosisStub {

    public void withDefaults() {

        stubFor(post(urlEqualTo("/docmosis"))
                .withMultipartRequestBody(aMultipart().matchingType(MatchingType.ALL))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody(random(100).getBytes())));
    }
}
