package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DocmosisDocumentGeneratorTest {

    private static final String DOCMOSIS_ACCESS_KEY = "ABC";
    private static final String DOCMOSIS_ENDPOINT = "http://docmosis";
    private static final String DOCMOSIS_RENDER_URI = "/rs/render";
    @Mock private ObjectMapper objectMapper;
    @Mock private RestTemplate restTemplate;

    private String fileName = "appeal-form";
    private String fileExtension = "PDF";
    private String templateName = "APPEAL_SUBMISSION.docx";
    @Mock private Map<String, Object> templateFieldValues;

    private final String serializedTemplateFieldValues = "{field-values}";

    private DocmosisDocumentGenerator docmosisDocumentGenerator;

    @Before
    public void setUp() {

        docmosisDocumentGenerator =
            new DocmosisDocumentGenerator(
                DOCMOSIS_ACCESS_KEY,
                DOCMOSIS_ENDPOINT,
                DOCMOSIS_RENDER_URI,
                objectMapper,
                restTemplate
            );
    }

    @Test
    public void should_call_docmosis_to_generate_document() throws JsonProcessingException {

        when(objectMapper.writeValueAsString(templateFieldValues)).thenReturn(serializedTemplateFieldValues);

        byte[] expectedDocumentData = "pdf-data".getBytes();

        doReturn(expectedDocumentData)
            .when(restTemplate)
            .postForObject(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_RENDER_URI),
                any(HttpEntity.class),
                any()
            );

        final Resource actualDocumentResource = docmosisDocumentGenerator.generate(
            fileName,
            fileExtension,
            templateName,
            templateFieldValues
        );

        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).postForObject(
            eq(DOCMOSIS_ENDPOINT + DOCMOSIS_RENDER_URI),
            httpEntityCaptor.capture(),
            any()
        );

        HttpEntity actualHttpEntity = httpEntityCaptor.getAllValues().get(0);

        HttpHeaders actualHeaders = actualHttpEntity.getHeaders();
        MultiValueMap actualBody = (MultiValueMap) actualHttpEntity.getBody();

        assertEquals(MediaType.MULTIPART_FORM_DATA, actualHeaders.getContentType());
        assertEquals(DOCMOSIS_ACCESS_KEY, actualBody.getFirst("accessKey"));
        assertEquals("appeal-form.PDF", actualBody.getFirst("outputName"));
        assertEquals("pdf", actualBody.getFirst("outputFormat"));
        assertEquals("APPEAL_SUBMISSION.docx", actualBody.getFirst("templateName"));
        assertEquals(serializedTemplateFieldValues, actualBody.getFirst("data"));

        assertEquals("appeal-form.PDF", actualDocumentResource.getFilename());
        assertEquals(expectedDocumentData, ((ByteArrayResource) actualDocumentResource).getByteArray());
    }

    @Test
    public void should_throw_if_template_field_values_cannot_be_serialized() throws JsonProcessingException {

        doThrow(JsonProcessingException.class)
            .when(objectMapper)
            .writeValueAsString(any());

        assertThatThrownBy(() ->
            docmosisDocumentGenerator.generate(
                fileName,
                fileExtension,
                templateName,
                templateFieldValues
            ))
            .hasMessage("Cannot serialize template field values")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_if_no_data_returned() throws JsonProcessingException {

        when(objectMapper.writeValueAsString(templateFieldValues)).thenReturn(serializedTemplateFieldValues);

        doReturn(null)
            .when(restTemplate)
            .postForObject(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_RENDER_URI),
                any(HttpEntity.class),
                any()
            );

        assertThatThrownBy(() ->
            docmosisDocumentGenerator.generate(
                fileName,
                fileExtension,
                templateName,
                templateFieldValues
            ))
            .hasMessage("No data returned from docmosis for file: appeal-form")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void wraps_http_server_exception_when_calling_docmosis() {
        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        when(restTemplate
            .postForObject(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_RENDER_URI),
                any(HttpEntity.class),
                any()
            ))
            .thenThrow(underlyingException);

        assertThatThrownBy(() -> docmosisDocumentGenerator.generate(
            fileName,
            fileExtension,
            templateName,
            templateFieldValues)
        ).isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessage("Couldn't generate asylum case documents with docmosis");
    }

    @Test
    public void wraps_http_client_exception_when_calling_docmosis() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        when(restTemplate
            .postForObject(
                eq(DOCMOSIS_ENDPOINT + DOCMOSIS_RENDER_URI),
                any(HttpEntity.class),
                any()
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> docmosisDocumentGenerator.generate(
            fileName,
            fileExtension,
            templateName,
            templateFieldValues)
        ).isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessage("Couldn't generate asylum case documents with docmosis");
    }
}
