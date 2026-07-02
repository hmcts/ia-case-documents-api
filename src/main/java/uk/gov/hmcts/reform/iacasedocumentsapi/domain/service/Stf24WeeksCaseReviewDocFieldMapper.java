package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.APPEAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.APPELLANT_FULL_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_14;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_14_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_42;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_42_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_56;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DAYS_56_FROM_DATE_OF_DIRECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.DECISION_SENT_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.D_MMM_YYYY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.PRACTICE_DIRECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.WEEKS_DEADLINE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.getAppellantFamilyName;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.getAppellantGivenName;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.Stf24WeeksUtils.populateStatutoryTimeFrame24wDate;

@Service
public class Stf24WeeksCaseReviewDocFieldMapper {


    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Stf24WeeksCaseReviewDocFieldMapper.class);

    public Stf24WeeksCaseReviewDocFieldMapper() {

    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        LOGGER.info("Mapping field values for case ID: {}", caseDetails.getId());
        final Map<String, Object> fieldValues = new HashMap<>();
        /// STF 24w
        LocalDate now = LocalDate.now();
        fieldValues.put(PRACTICE_DIRECTION, now.format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_14_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_14).format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_42_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_42).format(ofPattern(D_MMM_YYYY)));
        fieldValues.put(DAYS_56_FROM_DATE_OF_DIRECTION, now.plusDays(DAYS_56).format(ofPattern(D_MMM_YYYY)));
        String familyName = getAppellantFamilyName(asylumCase);
        String givenNames = getAppellantGivenName(asylumCase);

        fieldValues.put("appellantGivenNames", givenNames);
        fieldValues.put("appellantFamilyName", familyName);
        fieldValues.put(APPELLANT_FULL_NAME, (givenNames + " " + familyName).trim());
        fieldValues.put(WEEKS_DEADLINE, populateStatutoryTimeFrame24wDate(asylumCase));
        fieldValues.put(DECISION_SENT_DATE, Stf24WeeksUtils.getHomeOfficeDecisionDate(asylumCase));
        fieldValues.put(APPEAL_RECEIVED_DATE, Stf24WeeksUtils.getAppealReceivedDate(asylumCase));
        /// STF24W

        return fieldValues;
    }


}
