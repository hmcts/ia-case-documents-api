package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.roleassignment;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleAssignmentResource;


@FeignClient(
    name = "role-assignment-service-api",
    url = "${role-assignment-service.url}"
)
public interface RoleAssignmentApi {
    @PostMapping(value = "/am/role-assignments/query", consumes = "application/json")
    RoleAssignmentResource queryRoleAssignments(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody QueryRequest queryRequest
    );

}
