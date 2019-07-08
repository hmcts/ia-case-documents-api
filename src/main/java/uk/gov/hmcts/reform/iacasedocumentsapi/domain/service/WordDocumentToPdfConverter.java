package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.io.File.createTempFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.FileType;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocmosisDocumentConversionClient;

@Service
public class WordDocumentToPdfConverter {

    private final DocmosisDocumentConversionClient docmosisDocumentConversionClient;

    public WordDocumentToPdfConverter(DocmosisDocumentConversionClient docmosisDocumentConversionClient) {
        this.docmosisDocumentConversionClient = docmosisDocumentConversionClient;
    }

    public File convertResourceToPdf(Resource resource) {

        File tempPdfFile;

        try {

            InputStream wordDocumentInputStream = resource.getInputStream();

            File tempWordDocumentFile = createTempFile(
                "tmp_",
                ".docx");

            Files.copy(
                wordDocumentInputStream,
                tempWordDocumentFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(wordDocumentInputStream);

            byte[] convertedBinary = docmosisDocumentConversionClient.convert(
                tempWordDocumentFile,
                FileType.PDF);

            tempPdfFile = createTempFile(
                "tmp_",
                ".pdf");

            Files.write(tempPdfFile.toPath(), convertedBinary);

        } catch (IOException e) {
            throw new IllegalStateException("Unable to convert the document to a pdf", e);
        }

        return tempPdfFile;
    }
}
