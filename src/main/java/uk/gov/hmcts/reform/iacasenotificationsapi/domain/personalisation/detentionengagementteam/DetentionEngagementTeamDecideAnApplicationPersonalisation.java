package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamDecideAnApplicationPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamDecideAnApplicationApplicantTemplateId;
    private final MakeAnApplicationService makeAnApplicationService;
    private final DetentionEmailService detEmailService;

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;

    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamDecideAnApplicationPersonalisation(
        @Value("${govnotify.template.decideAnApplication.applicant.detentionEngagementTeam.email}") String detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
        CustomerServicesProvider customerServicesProvider,
        MakeAnApplicationService makeAnApplicationService,
        DetentionEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamDecideAnApplicationApplicantTemplateId = detentionEngagementTeamDecideAnApplicationApplicantTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamDecideAnApplicationApplicantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON)) {
            return Collections.singleton(detEmailService.getDetentionEmailAddress(asylumCase));
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<MakeAnApplication> optionalMakeAnApplication = getMakeAnApplication(asylumCase);
        MakeAnApplication makeAnApplication = optionalMakeAnApplication.orElseThrow(() -> new IllegalStateException("MakeAnApplication is not present"));
        boolean isRefused = makeAnApplication.getDecision().equals("Refused");
        ImmutableMap.Builder<String, Object> personalizationBuilder = ImmutableMap
            .<String, Object>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", getApplicationDecidedLetterJsonObject(asylumCase));

        if (isRefused) {
            if (isAcceleratedDetainedAppeal(asylumCase)) {
                personalizationBuilder.put("form", "*IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)");
                personalizationBuilder.put("formLink", "https://www.gov.uk/government/publications/make-an-application-accelerated-detained-appeal-form-iaft-ada4");
            } else {
                personalizationBuilder.put("form", "*IAFT-DE4: Make an application – Detained appeal");
                personalizationBuilder.put("formLink", "https://www.gov.uk/government/publications/make-an-application-detained-appeal-form-iaft-de4");
            }
        } else {
            personalizationBuilder.put("form", "");
            personalizationBuilder.put("formLink", "");
        }
        return personalizationBuilder.build();
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

    private JSONObject getApplicationDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Appeal decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Appeal decision Letter in compatible format");
        }
    }
}

