package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamRequestResponseAmendPersonalisation implements EmailWithLinkNotificationPersonalisation {

    @Value("${govnotify.emailPrefix.adaByPost}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaByPost}")
    private String nonAdaPrefix;

    private final String detentionEngagementTeamRequestResponseAmendTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamRequestResponseAmendPersonalisation(
            @Value("${govnotify.template.amendRespondentEvidenceDirection.detentionEngagementTeam.email}") String detentionEngagementTeamRequestResponseAmendTemplateId,
            PersonalisationProvider personalisationProvider,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamRequestResponseAmendTemplateId = detentionEngagementTeamRequestResponseAmendTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamRequestResponseAmendTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONSE_AMEND_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, Object> personalizationBuilder = ImmutableMap
                .<String, Object>builder()
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("documentLink", getRequestResponseAmendLetterJsonObject(asylumCase));

        return personalizationBuilder.build();
    }

    private JSONObject getRequestResponseAmendLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, AMEND_HOME_OFFICE_APPEAL_RESPONSE));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get amend Home Office response Letter in compatible format", e);
            throw new IllegalStateException("Failed to get amend Home Office response Letter in compatible format");
        }
    }
}
