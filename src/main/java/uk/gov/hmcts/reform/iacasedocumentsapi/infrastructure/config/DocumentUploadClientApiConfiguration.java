package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;


@Configuration
public class DocumentUploadClientApiConfiguration {

    @Bean
    @Primary
    public CaseDocumentClient documentUploadClientApi(
            CaseDocumentClientApi caseDocumentClientApi
    ) {
        return new CaseDocumentClient(caseDocumentClientApi);
    }
}
