package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2.IdamAuthoritiesConverter.TOKEN_NAME;

import com.google.common.collect.Lists;
import feign.FeignException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class IdamAuthoritiesConverterTest {

    @Mock
    private org.springframework.security.oauth2.jwt.Jwt jwt;

    @Mock
    private IdamApi idamApi;

    @Mock
    private UserInfo userInfo;

    private String tokenValue = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIj"
                                + "oiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSI"
                                + "sImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb"
                                + "3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI"
                                + "6MCwiYXVkaXRUcmFja2luZ0lkIjoiZDg3ODI3ODQtM"
                                + "WU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXN"
                                + "zIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hd"
                                + "XRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc19"
                                + "0b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhd"
                                + "XRoR3JhbnRJZCI6IjNjMWMzNjFkLTRlYzUtNGY0NS1"
                                + "iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZF9nY"
                                + "XRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF9"
                                + "0eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVua"
                                + "WQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1"
                                + "lIjoxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsI"
                                + "mV4cCI6MTU4NDU1NTE3MiwiaWF0IjoxNTg0NTI2Mzc"
                                + "yLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjN"
                                + "DMzYTQtZmRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU"
                                + "5In0.WP8ATcHMmdtG2W443aqNz3ES6-Bqng0IKjTQf"
                                + "bndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT"
                                + "8Ub6DjstnDz3VPmR3Cbdk5QJBdAsQ0ah9w-duS8SA_d"
                                + "lzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfgB9ZTLhe"
                                + "nVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWq"
                                + "lhrM--jPGRCweDiFe-ldi4EuhIxGbkPjyWwsdcgmYfIu"
                                + "FrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe4rFN6WV"
                                + "yHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";

    private IdamAuthoritiesConverter idamAuthoritiesConverter;

    @Test
    public void should_return_correct_granted_authority_collection() {

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        when(userInfo.getRoles()).thenReturn(Lists.newArrayList("caseworker-ia", "caseworker-ia-caseofficer"));
        when(idamApi.userInfo("Bearer " + tokenValue)).thenReturn(userInfo);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        List<GrantedAuthority> expectedGrantedAuthorities = Lists.newArrayList(
            new SimpleGrantedAuthority("caseworker-ia"),
            new SimpleGrantedAuthority("caseworker-ia-caseofficer")
        );

        Collection<GrantedAuthority> grantedAuthorities = idamAuthoritiesConverter.convert(jwt);

        verify(idamApi).userInfo("Bearer " + tokenValue);

        assertEquals(expectedGrantedAuthorities, grantedAuthorities);
    }

    @Test
    public void should_return_empty_list_when_token_is_missing() {

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    public void should_return_empty_list_when_user_info_does_not_contain_roles() {

        when(userInfo.getRoles()).thenReturn(Lists.newArrayList());
        when(idamApi.userInfo("Bearer " + tokenValue)).thenReturn(userInfo);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    public void should_throw_exception_when_auth_service_unavailable() {

        when(idamApi.userInfo("Bearer " + tokenValue)).thenThrow(FeignException.class);

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            () -> idamAuthoritiesConverter.convert(jwt)
        );
        assertEquals("Could not get user details from IDAM", thrown.getMessage());
    }
}
