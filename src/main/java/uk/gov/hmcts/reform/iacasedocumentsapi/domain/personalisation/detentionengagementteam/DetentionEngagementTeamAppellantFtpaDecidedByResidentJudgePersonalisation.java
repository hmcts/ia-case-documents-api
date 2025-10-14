package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@Slf4j
@Service
public class DetentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation implements EmailWithLinkNotificationPersonalisation {
    @Value("${linksToForms.iaut1}")
    private String iaut1FormUrl;
    private final String internalAppellantFtpaDecidedByResidentJudgeLetterTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String adaPrefix;
    private String nonAdaPrefix;

    public DetentionEngagementTeamAppellantFtpaDecidedByResidentJudgePersonalisation(
            @Value("${govnotify.template.ftpaDecidedByResidentJudge.detentionEngagementTeam.email}") String internalAppellantFtpaDecidedByResidentJudgeLetterTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            PersonalisationProvider personalisationProvider
    ) {
        this.internalAppellantFtpaDecidedByResidentJudgeLetterTemplateId = internalAppellantFtpaDecidedByResidentJudgeLetterTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return internalAppellantFtpaDecidedByResidentJudgeLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (!isAppellantInDetention(asylumCase) || detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_APPELLANT_FTPA_DECIDED_BY_RESIDENT_JUDGE_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<FtpaDecisionOutcomeType> ftpaAppellantDecisionOutcomeType = asylumCase
                .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (ftpaAppellantDecisionOutcomeType.isEmpty()) {
            throw new RequiredFieldMissingException("FTPA decision not found");
        }

        String formLinkForTemplateIfRequired = "";

        if (List.of(FTPA_PARTIALLY_GRANTED, FTPA_REFUSED).contains(ftpaAppellantDecisionOutcomeType.get())) {
            formLinkForTemplateIfRequired = "*IAUT1: Application for permission to appeal from First-tier Tribunal\n" + iaut1FormUrl;
        }

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedReviewHomeOfficeResponseLetterInJsonObject(asylumCase))
                .put("formLinkForTemplateIfRequired", formLinkForTemplateIfRequired)
                .build();
    }

    private JSONObject getInternalDetainedReviewHomeOfficeResponseLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_APPELLANT_FTPA_DECIDED_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get FTPA decision letter in compatible format", e);
            throw new IllegalStateException("Failed to get FTPA decision letter in compatible format");
        }
    }
}
