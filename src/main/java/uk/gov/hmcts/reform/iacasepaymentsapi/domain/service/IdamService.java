package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;

@Slf4j
@Component
public class IdamService {

    private final IdamApi idamApi;
    private final RoleAssignmentService roleAssignmentService;
    public static final List<String> amOnboardedRoles =
        List.of("caseworker-ia-caseofficer", "caseworker-ia-iacjudge", "caseworker-ia-admofficer");

    public IdamService(
        IdamApi idamApi,
        RoleAssignmentService roleAssignmentService
    ) {
        this.idamApi = idamApi;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        UserInfo userInfo = idamApi.userInfo(accessToken);
        List<String> amRoles = Collections.emptyList();
        List<String> idamRoles = userInfo.getRoles() == null ?
            Collections.emptyList() :
            userInfo.getRoles();
        try {
            amRoles = roleAssignmentService.getAmRolesFromUser(userInfo.getUid(), accessToken);
        } catch (Exception e) {
            if (idamRoles.stream().anyMatch(amOnboardedRoles::contains)) {
                log.error("Error fetching AM roles for user: {}", userInfo.getUid(), e);
            }
        }
        List<String> roles = Stream.concat(amRoles.stream(), idamRoles.stream()).toList();
        userInfo.setRoles(roles);
        return userInfo;
    }
}
