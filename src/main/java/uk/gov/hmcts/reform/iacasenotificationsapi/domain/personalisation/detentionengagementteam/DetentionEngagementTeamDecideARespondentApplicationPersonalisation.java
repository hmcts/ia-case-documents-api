package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamDecideARespondentApplicationPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamDecideARespondentApplicationApplicantTemplateId;
    private final DetEmailService detEmailService;

    @Value("${govnotify.emailPrefix.adaByPost}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaByPost}")
    private String nonAdaPrefix;

    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamDecideARespondentApplicationPersonalisation(
            @Value("${govnotify.template.decideARespondentApplication.detentionEngagementTeam.email}") String detentionEngagementTeamDecideARespondentApplicationApplicantTemplateId,
            CustomerServicesProvider customerServicesProvider,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamDecideARespondentApplicationApplicantTemplateId = detentionEngagementTeamDecideARespondentApplicationApplicantTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamDecideARespondentApplicationApplicantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_A_RESPONDENT_APPLICATION_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, Object> personalizationBuilder = ImmutableMap
                .<String, Object>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getApplicationDecidedLetterJsonObject(asylumCase));

        return personalizationBuilder.build();
    }

    private JSONObject getApplicationDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get respondent application decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get respondent application decision Letter in compatible format");
        }
    }
}
