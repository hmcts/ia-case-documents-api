package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.FileType;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocmosisDocumentConversionClient;

@ExtendWith(MockitoExtension.class)
public class WordDocumentToPdfConverterTest {

    @Mock
    private DocmosisDocumentConversionClient docmosisDocumentConversionClient;
    @Mock Resource resource;
    @Mock IOException ioException;
    @Mock InputStream inputStream;

    private WordDocumentToPdfConverter wordDocumentToPdfConverter;
    private ClassLoader classLoader = getClass().getClassLoader();

    @BeforeEach
    public void setUp() throws IOException {
        wordDocumentToPdfConverter = new WordDocumentToPdfConverter(
            docmosisDocumentConversionClient);
    }

    @Test
    public void handles_ioexception() throws IOException {

        when(resource.getInputStream()).thenReturn(inputStream);

        when(resource.getInputStream()).thenThrow(ioException);

        assertThatThrownBy(() -> wordDocumentToPdfConverter.convertResourceToPdf(resource))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to convert the document to a pdf")
            .hasCause(ioException);
    }

    @Test
    public void handles_docx_files() throws IOException {
        File docxFile = new File(
            classLoader.getResource(
                "draft-doc.docx").getPath());

        ByteArrayResource byteArrayResource =
            getByteArrayResource(
                docxFile,
                "some-word-doc.docx");

        byte[] convertedBytes = someRandomBytes();

        when(docmosisDocumentConversionClient.convert(
            any(File.class),
            eq(FileType.PDF))).thenReturn(convertedBytes);


        File pdf = wordDocumentToPdfConverter.convertResourceToPdf(byteArrayResource);

        //assertEquals(readAllBytes(pdf.toPath()), convertedBytes);
    }

    @Test
    public void convertsDocToPdf() throws IOException {

        File docFile = new File(
            classLoader.getResource(
                "draft-doc.doc").getPath());

        ByteArrayResource byteArrayResource =
            getByteArrayResource(
                docFile,
                "some-word-doc.doc");

        byte[] convertedBytes = someRandomBytes();

        when(docmosisDocumentConversionClient.convert(
            any(File.class),
            eq(FileType.PDF))).thenReturn(convertedBytes);


        File pdf = wordDocumentToPdfConverter.convertResourceToPdf(byteArrayResource);

        //assertEquals(readAllBytes(pdf.toPath()), convertedBytes);
    }

    private ByteArrayResource getByteArrayResource(
        File file,
        String filename
    ) {
        byte[] byteArray = new byte[0];

        try {
            byteArray = readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayResource(byteArray) {

            @Override
            public File getFile() {
                return file;
            }

            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private byte[] someRandomBytes() {

        byte[] b = new byte[20];
        new Random().nextBytes(b);

        return b;
    }
}
