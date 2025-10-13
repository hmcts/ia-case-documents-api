package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.roleassignment.RoleAssignmentApi;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleAssignmentServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private RoleAssignmentApi roleAssignmentApi;
    @InjectMocks
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private CaseDetails<CaseData> caseDetails;
    private final String serviceToken = "serviceToken";

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceToken);

        when(caseDetails.getId()).thenReturn(1234567890L);
    }

    @Test
    void getCaseRolesForUserTest() {
        String userId = "userId";
        Assignment assignment1 = Assignment.builder()
            .roleName(RoleName.CTSC_TEAM_LEADER)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();
        Assignment assignment2 = Assignment.builder()
            .roleName(RoleName.CTSC)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();
        Assignment assignment3 = Assignment.builder()
            .roleName(RoleName.HEARING_CENTRE_ADMIN)
            .roleType(RoleType.CASE)
            .actorId(userId)
            .build();
        Map<String, Object> requestBody = Map.of(
            "actorId", Collections.singletonList(userId),
            "roleType", Collections.singletonList(RoleType.ORGANISATION),
            "attributes", Collections.singletonMap("jurisdiction", Collections.singletonList("IA"))
        );

        String accessToken = "accessToken";
        when(roleAssignmentApi.queryRoleAssignments(
            eq(accessToken),
            eq(serviceToken),
            anyMap()
        )).thenReturn(new RoleAssignmentResource(List.of(assignment1, assignment2, assignment3)));

        List<String> roles = roleAssignmentService.getAmRolesFromUser(userId, accessToken);

        verify(roleAssignmentApi).queryRoleAssignments(
            eq(accessToken),
            eq(serviceToken),
            eq(requestBody)
        );
        assertTrue(roles.contains(RoleName.CTSC_TEAM_LEADER.getValue()));
        assertTrue(roles.contains(RoleName.CTSC.getValue()));
        assertTrue(roles.contains(RoleName.HEARING_CENTRE_ADMIN.getValue()));
    }
}
