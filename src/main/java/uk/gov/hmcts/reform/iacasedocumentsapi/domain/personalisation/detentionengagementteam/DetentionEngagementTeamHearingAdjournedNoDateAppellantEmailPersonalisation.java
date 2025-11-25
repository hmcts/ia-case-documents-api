package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.DETAINED_APPEAL_ADJOURN_HEARING_WITHOUT_DATE_IRC_PRISON_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInFacilityType;

@Slf4j
@Service
public class DetentionEngagementTeamHearingAdjournedNoDateAppellantEmailPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamTemplateId;
    private final String nonAdaPrefix;
    private final String ctscEmailAddress;
    private final DetentionEmailService detentionEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamHearingAdjournedNoDateAppellantEmailPersonalisation(
            @Value("${govnotify.template.adjournHearingWithoutDate.detentionEngagementTeam.appellant.email}") String detentionEngagementTeamTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetentionEmailService detentionEmailService,
            @Value("${ctscEmailAddress}") String ctscEmailAddress,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamTemplateId = detentionEngagementTeamTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.ctscEmailAddress = ctscEmailAddress;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DETAINED_APPEAL_ADJOURN_HEARING_NO_DATE_APPELLANT_LETTER";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isDetainedInFacilityType(asylumCase, DetentionFacility.IRC)) {
            return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
        } else {
            return Collections.singleton(ctscEmailAddress);
        }
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getRemissionGrantedInTimeLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getRemissionGrantedInTimeLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, DETAINED_APPEAL_ADJOURN_HEARING_WITHOUT_DATE_IRC_PRISON_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal 'Home Office to upload bundle' Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal 'Home Office to upload bundle' Letter in compatible format");
        }
    }
}
