package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.UserInfo;

@Slf4j
@Component
public class IdamService {

    private final IdamApi idamApi;
    private final RoleAssignmentService roleAssignmentService;

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
        try {
            amRoles = roleAssignmentService.getAmRolesFromUser(userInfo.getUid(), accessToken);
        } catch (Exception e) {
            log.error("Error fetching AM roles for user: {}", userInfo.getUid(), e);
        }
        List<String> roles = Stream.concat(amRoles.stream(), userInfo.getRoles().stream()).toList();
        userInfo.setRoles(roles);
        return userInfo;
    }
}
