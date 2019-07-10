package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class AppealSubmissionTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final String templateName;
    private final StringProvider stringProvider;

    public AppealSubmissionTemplate(
        @Value("${appealSubmissionDocument.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("CREATED_DATE", caseDetails.getCreatedDate().format(DOCUMENT_DATE_FORMAT));
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeDecisionDate", formatDateForRendering(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class).orElse("")));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("appellantDateOfBirth", formatDateForRendering(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("")));

        Optional<String> optionalAppealType = asylumCase.read(APPEAL_TYPE);

        if (optionalAppealType.isPresent()) {

            String appealType = optionalAppealType.get();

            fieldValues.put(
                "appealType",
                stringProvider.get("appealType", appealType).orElse("")
            );
        }

        fieldValues.put("newMatters", asylumCase.read(NEW_MATTERS, String.class).orElse(""));

        if (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.read(APPELLANT_ADDRESS);

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                fieldValues.put(
                    "appellantAddress",
                    ImmutableMap
                        .builder()
                        .put("appellantAddressLine1", appellantAddress.getAddressLine1().orElse(""))
                        .put("appellantAddressLine2", appellantAddress.getAddressLine2().orElse(""))
                        .put("appellantAddressLine3", appellantAddress.getAddressLine3().orElse(""))
                        .put("appellantAddressPostTown", appellantAddress.getPostTown().orElse(""))
                        .put("appellantAddressCounty", appellantAddress.getCounty().orElse(""))
                        .put("appellantAddressPostCode", appellantAddress.getPostCode().orElse(""))
                        .put("appellantAddressCountry", appellantAddress.getCountry().orElse(""))
                        .build()
                );
            }
        }

        Optional<List<IdValue<Map<String, String>>>> appellantNationalities = asylumCase
                .read(APPELLANT_NATIONALITIES);

        fieldValues.put(
            "appellantNationalities",
            appellantNationalities
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("code"))
                .map(idValue -> idValue.getValue().get("code"))
                .map(code -> stringProvider.get("isoCountries", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(nationality -> ImmutableMap.of("nationality", nationality))
                .collect(Collectors.toList())
        );

        Optional<List<String>> groundsOfAppealForDisplay = asylumCase
                .read(APPEAL_GROUNDS_FOR_DISPLAY);

        fieldValues.put(
            "appealGrounds",
            groundsOfAppealForDisplay
                .orElse(Collections.emptyList())
                .stream()
                .map(code -> stringProvider.get("appealGrounds", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(appealGround -> ImmutableMap.of("appealGround", appealGround))
                .collect(Collectors.toList())
        );

        Optional<List<IdValue<Map<String, String>>>> otherAppeals = asylumCase
                .read(OTHER_APPEALS);

        fieldValues.put(
            "otherAppeals",
            otherAppeals
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("value"))
                .map(idValue -> idValue.getValue().get("value"))
                .collect(joining(", "))
        );

        fieldValues.put("applicationOutOfTimeExplanation", asylumCase.read(APPLICATION_OUT_OF_TIME_EXPLANATION, String.class).orElse(""));
        fieldValues.put("submissionOutOfTime", asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(YesOrNo.NO));
        fieldValues.put(
            "applicationOutOfTimeDocumentName",
            asylumCase.read(APPLICATION_OUT_OF_TIME_DOCUMENT, Document.class)
                .map(Document::getDocumentFilename)
                .orElse("")
        );

        return fieldValues;
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }
}
