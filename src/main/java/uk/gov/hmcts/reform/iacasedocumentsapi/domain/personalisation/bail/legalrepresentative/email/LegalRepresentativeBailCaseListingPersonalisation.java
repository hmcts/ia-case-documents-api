package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.BailCaseUtils.isBailConditionalGrant;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Service
public class LegalRepresentativeBailCaseListingPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String caseListingInitialWithLegalRepPersonalisationTemplateId;
    private final String caseListingRelistingWithLegalRepPersonalisationTemplateId;
    private final String caseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DateTimeExtractor dateTimeExtractor;

    public LegalRepresentativeBailCaseListingPersonalisation(
        @NotNull(message = "caseListingWithLegalRepPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.caseListing.initial.withLegalRep.email}") String caseListingInitialWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.relisting.withLegalRep.email}") String caseListingRelistingWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.conditionalBailRelisting.withLegalRep.email}") String caseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId,
        HearingDetailsFinder hearingDetailsFinder,
        DateTimeExtractor dateTimeExtractor
    ) {
        this.caseListingInitialWithLegalRepPersonalisationTemplateId = caseListingInitialWithLegalRepPersonalisationTemplateId;
        this.caseListingRelistingWithLegalRepPersonalisationTemplateId = caseListingRelistingWithLegalRepPersonalisationTemplateId;
        this.caseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId = caseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    @Override
    public String getTemplateId(BailCase bailCase) {

        ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElseThrow(() -> new IllegalStateException("Listing Event is not present"));

        return switch (listingEvent) {
            case INITIAL -> caseListingInitialWithLegalRepPersonalisationTemplateId;
            case RELISTING -> isBailConditionalGrant(bailCase) ?
                caseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId :
                caseListingRelistingWithLegalRepPersonalisationTemplateId;
        };
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_CASE_LISTING_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingCentre", hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase))
            .build();
    }
}
