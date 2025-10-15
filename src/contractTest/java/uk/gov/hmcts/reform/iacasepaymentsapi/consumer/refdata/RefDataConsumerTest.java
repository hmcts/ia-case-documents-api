package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.refdata;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "referenceData_organisationalExternalPbas", port = "8991")
@ContextConfiguration(
    classes = {RefDataConsumerApplication.class}
)
@TestPropertySource(
    properties = {"rd-professional.api.url=localhost:8991"}
)
@PactFolder("pacts")
public class RefDataConsumerTest {

    @Autowired
    RefDataApi refDataApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "ServiceToken";
    static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";


    @Pact(provider = "referenceData_organisationalExternalPbas", consumer = "ia_casePaymentsApi")
    public V4Pact generatePactFragment(PactDslWithProvider builder) {
        return builder
            .given("Pbas organisational data exists for identifier " + ORGANISATION_EMAIL)
            .uponReceiving("a request for information for that organisation's pbas")
            .method("GET")
            .path("/refdata/external/v1/organisations/pbas")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER,
                     AUTHORIZATION_TOKEN, "UserEmail", ORGANISATION_EMAIL)
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact(V4Pact.class);
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.object("organisationEntityResponse", or ->
                or.stringType("organisationIdentifier", ORGANISATION_EMAIL)
                .stringType("name", "name")
                .stringType("status","ACTIVE")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", true)
                .stringType("companyNumber", "companyNumber")
                .stringType("companyUrl", "companyUrl")
                .object("superUser", su -> su
                    .stringType("firstName", "firstName")
                    .stringType("lastName", "lastName")
                    .stringType("email", "email@org.com"))
                .array("paymentAccount", pa ->
                    pa.stringType("paymentAccountA1"))
            );
        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPactResponse() {
        OrganisationResponse response = refDataApi.findOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
                                                                    ORGANISATION_EMAIL);
        assertEquals(response.getOrganisationEntityResponse().getOrganisationIdentifier(), ORGANISATION_EMAIL);

    }
}
