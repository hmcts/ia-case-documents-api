package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.CcdDataApi;

public class CcdCaseCreationTest {

    @Value("classpath:templates/start-appeal.json")
    protected Resource startAppeal;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    private long caseId;
    private String s2sToken;
    private String legalRepToken;
    private String legalRepUserId;
    public String paymentReference;

    private static final String jurisdiction = "IA";
    private static final String caseType = "Asylum";

    @Autowired
    private CcdDataApi ccdApi;

    public void shouldPayAndSubmitAppeal() {

        startAppeal();
        submitAppeal();
    }

    private void startAppeal() {

        legalRepToken = idamAuthProvider.getLegalRepToken();
        s2sToken = s2sAuthTokenGenerator.generate();
        legalRepUserId = idamAuthProvider.getUserId(legalRepToken);

        Map<String, Object> data = getStartAppealData();
        data.put("paAppealTypePaymentOption", "payNow");

        MapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventDetails startEventDetails =
            ccdApi.startCaseCreation(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, eventId);

        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        CaseDataContent content =
            new CaseDataContent(null, data, event, startEventDetails.getToken(), true);

        CaseDetails<AsylumCase> caseDetails =
            ccdApi.submitCaseCreation(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, content);

        caseId = caseDetails.getId();
    }

    private void submitAppeal() {

        Map<String, Object> data = new HashMap<>();

        final List<uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value> values = new ArrayList<>();
        values.add(
            new uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value("PBA0087535", "PBA0087535"));
        DynamicList pbaList = new DynamicList(values.get(0), values);

        data.put("paymentAccountList", pbaList);
        data.put("decisionHearingFeeOption", "decisionWithHearing");

        MapValueExpander.expandValues(data);

        String eventId = "payAndSubmitAppeal";
        StartEventDetails startEventDetails =
            ccdApi.startEvent(legalRepToken, s2sToken, legalRepUserId, jurisdiction,
                              caseType, String.valueOf(caseId), eventId);

        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        CaseDataContent content =
            new CaseDataContent(String.valueOf(caseId), data, event, startEventDetails.getToken(), true);

        SubmitEventDetails submitEventDetails =
            ccdApi.submitEvent(legalRepToken, s2sToken, String.valueOf(caseId), content);

        paymentReference = submitEventDetails.getData().get("paymentReference").toString();
    }

    private Map<String, Object> getStartAppealData() {

        Map<String, Object> data = Collections.emptyMap();

        try {
            data = new ObjectMapper()
                .readValue(asString(startAppeal), new TypeReference<Map<String, Object>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return data;
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long getCaseId() {
        return caseId;
    }
}
