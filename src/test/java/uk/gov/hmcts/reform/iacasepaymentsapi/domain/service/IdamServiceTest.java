package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamServiceTest {
    @Mock
    private IdamApi idamApi;
    @Mock
    private RoleAssignmentService roleAssignmentService;

    private IdamService idamService;

    @BeforeEach
    void setUp() {
        idamService = new IdamService(idamApi, roleAssignmentService);
    }

    @Test
    void getUserDetails_from_am_and_idam() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        List<String> expectedAmRoles = Arrays.asList("role-3", "role-4");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(expectedAmRoles);
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);
        List<String> expectedRoles = Stream.concat(expectedAmRoles.stream(), expectedIdamRoles.stream()).toList();

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @Test
    void getUserDetails_from_idam() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(Collections.emptyList());
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedIdamRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @ParameterizedTest
    @CsvSource({"empty", "null"})
    void getUserDetails_from_am(String expectedIdamRoles) {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedAmRoles = Arrays.asList("role-3", "role-4");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles.equals("null") ? null : Collections.emptyList(),
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(expectedAmRoles);
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedAmRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @Test
    void getUserDetails_logs_exception_when_role_assignment_service_fails() {
        Logger responseLogger = (Logger) LoggerFactory.getLogger(IdamService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenThrow(new NullPointerException("Role assignment service failed"));
        idamService.getUserInfo(expectedAccessToken);
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        assertEquals("Error fetching AM roles for user: 1234", logEvents.get(0).getFormattedMessage());

        verify(idamApi).userInfo(expectedAccessToken);
    }
}
