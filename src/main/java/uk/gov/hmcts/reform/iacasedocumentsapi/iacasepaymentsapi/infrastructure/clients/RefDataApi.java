package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;

@FeignClient(name = "rd-professional-api", url = "${rd-professional.api.url}")
public interface RefDataApi {

    @GetMapping("/refdata/external/v1/organisations/pbas")
    OrganisationResponse findOrganisation(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(name = "UserEmail") String email
    );
}
