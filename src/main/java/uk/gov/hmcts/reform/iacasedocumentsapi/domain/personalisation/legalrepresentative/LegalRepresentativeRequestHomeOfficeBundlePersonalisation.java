package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;

@Service
public class LegalRepresentativeRequestHomeOfficeBundlePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepEvidenceDirectionNonAdaTemplateId;
    private final String legalRepEvidenceDirectionAdaTemplateId;
    private final DirectionFinder directionFinder;

    public LegalRepresentativeRequestHomeOfficeBundlePersonalisation(
        @Value("${govnotify.template.requestRespondentEvidenceDirection.legalRep.email.nonAda}") String legalRepEvidenceDirectionNonAdaTemplateId,
        @Value("${govnotify.template.requestRespondentEvidenceDirection.legalRep.email.ada}") String legalRepEvidenceDirectionAdaTemplateId,
        DirectionFinder directionFinder) {

        this.legalRepEvidenceDirectionNonAdaTemplateId = legalRepEvidenceDirectionNonAdaTemplateId;
        this.legalRepEvidenceDirectionAdaTemplateId = legalRepEvidenceDirectionAdaTemplateId;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)
            ? legalRepEvidenceDirectionAdaTemplateId
            : legalRepEvidenceDirectionNonAdaTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_EVIDENCE_DIRECTION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("insertDate", directionDueDate)
            .build();
    }
}
