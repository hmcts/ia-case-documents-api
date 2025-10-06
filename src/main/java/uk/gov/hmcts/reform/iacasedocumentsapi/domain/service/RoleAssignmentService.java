package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

@Component
@Slf4j
public class RoleAssignmentService {
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final RoleAssignmentApi roleAssignmentApi;

    public RoleAssignmentService(AuthTokenGenerator serviceAuthTokenGenerator,
                                 RoleAssignmentApi roleAssignmentApi) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.roleAssignmentApi = roleAssignmentApi;
    }

    public List<String> getAmRolesFromUser(String actorId,
                                           String authorization) {
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(Collections.singletonList(actorId))
            .roleType(Collections.singletonList(RoleType.ORGANISATION))
            .build();
        RoleAssignmentResource roleAssignmentResource = roleAssignmentApi.queryRoleAssignments(
            authorization,
            serviceAuthTokenGenerator.generate(),
            queryRequest
        );
        return Optional.ofNullable(roleAssignmentResource.roleAssignmentResponse()).orElse(Collections.emptyList())
            .stream()
            .map(Assignment::getRoleName)
            .filter(roleName -> roleName != RoleName.UNKNOWN)
            .map(RoleName::getValue)
            .toList();
    }
}
