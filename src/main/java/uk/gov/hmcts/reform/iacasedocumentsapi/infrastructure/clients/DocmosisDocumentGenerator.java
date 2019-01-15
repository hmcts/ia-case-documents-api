package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;

@Service
public class DocmosisDocumentGenerator implements DocumentGenerator {

    private static final Logger LOG = getLogger(DocmosisDocumentGenerator.class);

    private final String docmosisAccessKey;
    private final String docmosisUrl;
    private final String docmosisRenderUri;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public DocmosisDocumentGenerator(
        @Value("${docmosis.accessKey}") String docmosisAccessKey,
        @Value("${docmosis.url}") String docmosisUrl,
        @Value("${docmosis.render.uri}") String docmosisRenderUri,
        ObjectMapper objectMapper,
        RestTemplate restTemplate
    ) {
        this.docmosisAccessKey = docmosisAccessKey;
        this.docmosisUrl = docmosisUrl;
        this.docmosisRenderUri = docmosisRenderUri;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public Resource generate(
        String fileName,
        String fileExtension,
        String templateName,
        Map<String, Object> templateFieldValues
    ) {
        final String fileNameWithExension = fileName + "." + fileExtension;
        final String serializedTemplateFieldValues = serializedTemplateFieldValues(templateFieldValues);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", docmosisAccessKey);
        body.add("outputName", fileNameWithExension);
        body.add("outputFormat", fileExtension.toLowerCase());
        body.add("templateName", templateName);
        body.add("data", serializedTemplateFieldValues);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String docmosisError = "";

        try {

            LOG.info("MediaType: {}", MediaType.MULTIPART_FORM_DATA_VALUE);
            LOG.info("endpoint: {}{}", docmosisUrl, docmosisRenderUri);
            LOG.info("outputName: {}", fileNameWithExension);
            LOG.info("templateName: {}", templateName);
            LOG.info("data: {}", serializedTemplateFieldValues);

            byte[] documentData =
                restTemplate
                    .postForObject(
                        docmosisUrl + docmosisRenderUri,
                        requestEntity,
                        byte[].class
                    );

            if (documentData == null) {
                throw new IllegalStateException("No data returned from docmosis for file: " + fileName);
            }

            return new ByteArrayResource(documentData) {
                @Override
                public String getFilename() {
                    return fileNameWithExension;
                }
            };

        } catch (RestClientException e) {
            docmosisError = e.getMessage();
            LOG.error("Docmosis error: " + docmosisError, e);
        }

        throw new IllegalStateException("Docmosis error: " + docmosisError);
    }

    private String serializedTemplateFieldValues(
        Map<String, Object> templateFieldValues
    ) {
        try {
            return objectMapper.writeValueAsString(templateFieldValues);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize template field values", e);
        }
    }
}
