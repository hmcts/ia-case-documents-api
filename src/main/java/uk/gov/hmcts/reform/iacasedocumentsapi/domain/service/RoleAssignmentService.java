package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

@Component
@Slf4j
public class RoleAssignmentService {
    public static final String ROLE_NAME = "tribunal-caseworker";
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final RoleAssignmentApi roleAssignmentApi;

    public RoleAssignmentService(AuthTokenGenerator serviceAuthTokenGenerator,
                                 RoleAssignmentApi roleAssignmentApi) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.roleAssignmentApi = roleAssignmentApi;
    }

    public List<String> getAmRolesFromUser(String actorId,
                                           String authorization) {
        RoleAssignmentResource roleAssignmentResource = roleAssignmentApi.getRoleAssignments(
            authorization,
            serviceAuthTokenGenerator.generate(),
            actorId
        );
        return Optional.ofNullable(roleAssignmentResource.roleAssignmentResponse()).orElse(Collections.emptyList())
            .stream()
            .map(Assignment::getRoleName)
            .map(RoleName::getValue)
            .toList();
    }
}
