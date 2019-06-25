package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;


@Slf4j
@RunWith(SpringRunner.class)
public class DocmosisConverterTest {


    private String DOCMOSIS_ACCESS_KEY = "";
    private String DOCMOSIS_ENDPOINT = "https://docmosis-development.platform.hmcts.net";
    private String CONVERT_PATH = "/rs/convert";
    private String DOC_FILE_NAME = "wordDocument.doc";
    private String FINAL_FILE_NAME = "result.pdf";

    private RestTemplate restTemplate = new RestTemplate();

    private DocumentUploader documentUploader;

    @Test
    public void should_call_convert_path_and_get_response() {

        byte[] documentData;
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(DOC_FILE_NAME);
        assertNotNull(url);
        File file = new File(url.getFile());
        assertTrue(file.exists());

        try {

            documentData =
                restTemplate
                    .postForObject(
                        DOCMOSIS_ENDPOINT + CONVERT_PATH,
                        createRequest(file),
                        byte[].class
                    );

        } catch (HttpClientErrorException clientEx) {

            log.error("\n\nResponse: " + clientEx.getResponseBodyAsString());

            throw new DocumentServiceResponseException(
                AlertLevel.P2,
                clientEx.getResponseBodyAsString(),
                clientEx
            );
        }

        Assert.assertNotNull(documentData);

        log.info("Response: {}" + Arrays.toString(documentData));


        Resource resource = new ByteArrayResource(documentData) {
            @Override
            public String getFilename() {
                return FINAL_FILE_NAME;
            }
        };

        //TODO - Upload to document managmement
        //documentUploader.upload(resource, MediaType.APPLICATION_PDF_VALUE);

    }

    private HttpEntity<MultiValueMap<String, Object>> createRequest(final File file) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", DOCMOSIS_ACCESS_KEY);
        body.add("outputName", FINAL_FILE_NAME);
        body.add("file", new FileSystemResource(file));

        return new HttpEntity<>(body, headers);
    }

}

