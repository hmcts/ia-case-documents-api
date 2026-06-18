package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import java.lang.reflect.Type;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;


@ControllerAdvice
@SuppressWarnings("unchecked")
@Slf4j
public class AsylumCaseRequestAdapter extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        Callback<AsylumCase> callback = (Callback<AsylumCase>) body;
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());

        RequestContextHolder.currentRequestAttributes().setAttribute("CCDCaseId", caseId, RequestAttributes.SCOPE_REQUEST);

        // Set in MDC for logging pattern
        MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, caseId);

        return body;
    }
}
