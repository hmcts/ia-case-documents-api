package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamHoFtpaDecidedGrantedTemplateId;
    private final String detentionEngagementTeamHoFtpaDecidedRefusedTemplateId;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaSubjectPrefix;

    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation(
        @Value("${govnotify.template.hoFtpaDecided.detentionEngagementTeam.granted.email}") String detentionEngagementTeamHoFtpaDecidedGrantedTemplateId,
        @Value("${govnotify.template.hoFtpaDecided.detentionEngagementTeam.refused.email}") String detentionEngagementTeamHoFtpaDecidedRefusedTemplateId,
        DetEmailService detEmailService,
        PersonalisationProvider personalisationProvider,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamHoFtpaDecidedGrantedTemplateId = detentionEngagementTeamHoFtpaDecidedGrantedTemplateId;
        this.detentionEngagementTeamHoFtpaDecidedRefusedTemplateId = detentionEngagementTeamHoFtpaDecidedRefusedTemplateId;
        this.detEmailService = detEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_HO_FTPA_DECIDED_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaRespondentDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        return ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_GRANTED)) ||
               ftpaRespondentDecisionOutcomeType.equals(Optional.of(FTPA_PARTIALLY_GRANTED))
            ? detentionEngagementTeamHoFtpaDecidedGrantedTemplateId
            : detentionEngagementTeamHoFtpaDecidedRefusedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, Object>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaSubjectPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
            .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
            .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_HO_FTPA_DECIDED_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get HO Ftpa decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get HO Ftpa decision Letter in compatible format");
        }
    }
}

