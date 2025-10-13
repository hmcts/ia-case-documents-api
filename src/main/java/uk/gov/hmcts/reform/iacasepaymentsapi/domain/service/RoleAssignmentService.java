package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

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

    @Retryable(retryFor = FeignException.class)
    public List<String> getAmRolesFromUser(String actorId,
                                           String authorization) {
        RoleAssignmentResource roleAssignmentResource = roleAssignmentApi.queryRoleAssignments(
            authorization,
            serviceAuthTokenGenerator.generate(),
            Map.of(
                "actorId", Collections.singletonList(actorId),
                "roleType", Collections.singletonList(RoleType.ORGANISATION),
                "attributes", Collections.singletonMap("jurisdiction", Collections.singletonList("IA"))
            )
        );
        return Optional.ofNullable(roleAssignmentResource.roleAssignmentResponse()).orElse(Collections.emptyList())
            .stream()
            .map(Assignment::getRoleName)
            .filter(roleName -> roleName != RoleName.UNKNOWN)
            .map(RoleName::getValue)
            .toList();
    }
}
