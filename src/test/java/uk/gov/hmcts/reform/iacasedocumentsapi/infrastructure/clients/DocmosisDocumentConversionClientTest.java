package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.FileType;

@RunWith(MockitoJUnitRunner.class)
public class DocmosisDocumentConversionClientTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock private HttpClientErrorException httpClientErrorException;

    @Captor private ArgumentCaptor<HttpEntity<Map<String, List>>> httpEntityArgumentCaptor;

    private String someAccessKey = "some-access-key";
    private String someEndpoint = "some-endpoint";
    private String someUri = "some-uri";
    private DocmosisDocumentConversionClient docmosisDocumentConversionClient;
    private File tempSourceFile;

    @Before
    public void setUp() throws IOException {

        docmosisDocumentConversionClient = new DocmosisDocumentConversionClient(
            someAccessKey,
            someEndpoint,
            someUri,
            restTemplate);

        tempSourceFile = File.createTempFile("some-file-name", ".abc");
    }

    @Test
    public void returns_converted_bytes() {

        byte[] someRandomBytes = someRandomBytes();

        when(restTemplate.postForObject(
            anyString(),
            any(HttpEntity.class),
            eq(byte[].class)))
                .thenReturn(someRandomBytes);

        byte[] convertedBytes = docmosisDocumentConversionClient.convert(tempSourceFile, FileType.PDF);

        verify(restTemplate, times(1))
            .postForObject(
                eq(someEndpoint + someUri),
                httpEntityArgumentCaptor.capture(),
                eq(byte[].class));

        HttpEntity<Map<String, List>> httpEntity = httpEntityArgumentCaptor.getValue();

        assertThat(httpEntity.getBody().get("accessKey").get(0)).isEqualTo(someAccessKey);
        assertThat(httpEntity.getBody().get("outputName").get(0)).isEqualTo("temp.pdf");
        assertThat(httpEntity.getBody().get("file").get(0))
            .extracting("file")
            .containsExactly(tempSourceFile);

        assertThat(convertedBytes).isEqualTo(convertedBytes);
    }

    @Test
    public void handles_http_exception() {

        when(restTemplate.postForObject(
            anyString(),
            any(HttpEntity.class),
            any())).thenThrow(httpClientErrorException);

        when(httpClientErrorException.getResponseBodyAsString())
            .thenReturn("some-response-body");

        assertThatThrownBy(() -> docmosisDocumentConversionClient.convert(tempSourceFile, FileType.PDF))
            .isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessage("some-response-body")
            .hasCause(httpClientErrorException);
    }

    private byte[] someRandomBytes() {

        byte[] b = new byte[20];
        new Random().nextBytes(b);

        return b;
    }
}