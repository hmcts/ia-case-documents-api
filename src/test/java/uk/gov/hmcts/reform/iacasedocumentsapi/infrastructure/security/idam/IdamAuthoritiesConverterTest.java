package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdamAuthoritiesConverter.REGISTRATION_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdamAuthoritiesConverter.TOKEN_NAME;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class IdamAuthoritiesConverterTest {

    @Mock
    private org.springframework.security.oauth2.jwt.Jwt jwt;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity<Map<String, Object>> responseEntity;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    private String userInfoUrl = "http://idamhost/o/userinfo";
    private String tokenValue = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjNDMzYTQtZmRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-Bqng0IKjTQfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3VPmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfgB9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDiFe-ldi4EuhIxGbkPjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";
    private Map<String, Object> userInfoResponse = ImmutableMap.<String, Object>builder()
        .put("roles", Lists.newArrayList("caseworker-ia", "caseworker-ia-caseofficer"))
        .build();

    private IdamAuthoritiesConverter idamAuthoritiesConverter;

    @Before
    public void setUp() {

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .userInfoUri(userInfoUrl)
            .clientId("someClientId")
            .redirectUriTemplate("someClientID")
            .authorizationUri("/someauthuri")
            .tokenUri("/sometokenuri")
            .build();

        when(clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID)).thenReturn(clientRegistration);

        when(responseEntity.getBody()).thenReturn(userInfoResponse);
        when(
            restTemplate.exchange(
                eq(userInfoUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenReturn(responseEntity);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(restTemplate, clientRegistrationRepository);
    }

    @Test
    public void should_return_correct_granted_authority_collection() {

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        List<GrantedAuthority> expectedGrantedAuthorities = Lists.newArrayList(
            new SimpleGrantedAuthority("caseworker-ia"),
            new SimpleGrantedAuthority("caseworker-ia-caseofficer")
        );

        Collection<GrantedAuthority> grantedAuthorities = idamAuthoritiesConverter.convert(jwt);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
            eq(userInfoUrl),
            eq(HttpMethod.GET),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity<Map<String, Object>> actualRequestEntity = requestEntityCaptor.getValue();

        assertEquals("Bearer " + tokenValue, actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(expectedGrantedAuthorities, grantedAuthorities);

    }

    @Test
    public void should_return_empty_list_when_token_is_missing() {

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    public void should_return_empty_list_when_user_info_does_not_contain_roles() {

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        when(responseEntity.getBody()).thenReturn(ImmutableMap.<String, Object>builder().build());

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    public void should_throw_exception_when_auth_service_unavailable() {

        when(jwt.containsClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        when(
            restTemplate.exchange(
                eq(userInfoUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(RestClientException.class);

        assertThatThrownBy(() -> idamAuthoritiesConverter.convert(jwt))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details from IDAM");
    }
}