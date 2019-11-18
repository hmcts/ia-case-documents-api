package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.FileType;

@Service
public class DocmosisDocumentConversionClient {

    private final String docmosisAccessKey;
    private final RestTemplate restTemplate;
    private final String documentConversionUrl;

    public DocmosisDocumentConversionClient(
        @Value("${docmosis.accessKey}") String docmosisAccessKey,
        @Value("${docmosis.endpoint}") String docmosisEndpoint,
        @Value("${docmosis.convert.uri}") String docmosisConvertUri,
        RestTemplate restTemplate
    ) {
        this.docmosisAccessKey = docmosisAccessKey;
        this.restTemplate = restTemplate;
        this.documentConversionUrl = docmosisEndpoint + docmosisConvertUri;
    }

    public byte[] convert(File sourceFile, FileType targetFileType) {

        try {

            return restTemplate
                .postForObject(
                    documentConversionUrl,
                    createRequest(sourceFile, "temp." + targetFileType.getValue()),
                    byte[].class
                );

        } catch (RestClientResponseException clientEx) {

            throw new DocumentServiceResponseException(
                clientEx.getResponseBodyAsString(),
                clientEx
            );
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequest(
        File file,
        String decisionAndReasonsFilename
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", docmosisAccessKey);
        body.add("outputName", decisionAndReasonsFilename);
        body.add("file", new FileSystemResource(file));

        return new HttpEntity<>(body, headers);
    }
}
