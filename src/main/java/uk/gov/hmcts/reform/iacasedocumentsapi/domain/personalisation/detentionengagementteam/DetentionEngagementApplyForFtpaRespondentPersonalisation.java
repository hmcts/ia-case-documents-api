package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_APPLY_FOR_FTPA_RESPONDENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementApplyForFtpaRespondentPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetApplyForFtpaRespondentTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetentionEmailService detentionEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final String adaPrefix;
    private final String nonAdaPrefix;

    public DetentionEngagementApplyForFtpaRespondentPersonalisation(
            @Value("${govnotify.template.applyForFtpa.homeOffice.detentionEngagementTeam.email}") String internalDetApplyForFtpaRespondentTemplateId,
            DocumentDownloadClient documentDownloadClient,
            DetentionEmailService detentionEmailService,
            PersonalisationProvider personalisationProvider,
            @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix) {
        this.internalDetApplyForFtpaRespondentTemplateId = internalDetApplyForFtpaRespondentTemplateId;
        this.documentDownloadClient = documentDownloadClient;
        this.detentionEmailService = detentionEmailService;
        this.personalisationProvider = personalisationProvider;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId() {
        return internalDetApplyForFtpaRespondentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_APPLY_FOR_FTPA_RESPONDENT";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedEditCaseListingLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalDetainedEditCaseListingLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_APPLY_FOR_FTPA_RESPONDENT));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained apply for FTPA respondent letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained apply for FTPA respondent letter in compatible format");
        }
    }
}
