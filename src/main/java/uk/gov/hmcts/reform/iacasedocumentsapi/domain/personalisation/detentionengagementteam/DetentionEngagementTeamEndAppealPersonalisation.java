package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.END_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
public class DetentionEngagementTeamEndAppealPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamEndAppealTemplateId;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaSubjectPrefix;

    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamEndAppealPersonalisation(
            @Value("${govnotify.template.endAppeal.detentionEngagementTeam.email}") String detentionEngagementTeamEndAppealTemplateId,
            DetEmailService detEmailService,
            PersonalisationProvider personalisationProvider, DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamEndAppealTemplateId = detentionEngagementTeamEndAppealTemplateId;
        this.detEmailService = detEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_END_APPEAL_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamEndAppealTemplateId;
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
                .put("formName", resolveFormNameAndLink(asylumCase).getLeft())
                .put("formLinkText", resolveFormNameAndLink(asylumCase).getRight())
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private ImmutablePair<String, String> resolveFormNameAndLink(AsylumCase asylumCase) {
        final String adaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
        final String nonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
        final String adaFormLink = "https://www.gov.uk/government/publications/make-an-application-accelerated-detained-appeal-form-iaft-ada4";
        final String nonAdaFormLink = "https://www.gov.uk/government/publications/make-an-application-detained-appeal-form-iaft-de4";

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            return new ImmutablePair<>(adaFormName, adaFormLink);
        } else {
            return new ImmutablePair<>(nonAdaFormName, nonAdaFormLink);
        }
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, END_APPEAL));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal End Appeal decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal End Appeal decision Letter in compatible format");
        }
    }
}
