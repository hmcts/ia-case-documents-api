package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;

@Configuration
public class CaseDocumentClientConfiguration {

    @Bean
    public CaseDocumentClient caseDocumentClient(CaseDocumentClientApi caseDocumentClientApi) {
        return new CaseDocumentClient(caseDocumentClientApi);
    }

}
