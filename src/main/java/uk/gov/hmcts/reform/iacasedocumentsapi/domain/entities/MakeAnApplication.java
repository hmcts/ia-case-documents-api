package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class MakeAnApplication {

    private String type;
    private String details;
    private List<IdValue<Document>> evidence;
    private String applicant;
    private String date;
    private String decision;
    private String state;
    private String applicantRole;
    private String decisionReason;
    private String decisionDate;
    private String decisionMaker;

    public MakeAnApplication() {

    }

    public MakeAnApplication(
            String applicant,
            String type,
            String details,
            List<IdValue<Document>> evidence,
            String date,
            String decision,
            String state,
            String applicantRole) {
        requireNonNull(applicant);
        requireNonNull(type);
        requireNonNull(details);
        requireNonNull(evidence);
        requireNonNull(date);
        requireNonNull(decision);
        requireNonNull(state);
        requireNonNull(applicantRole);

        this.applicant = applicant;
        this.type = type;
        this.details = details;
        this.evidence = evidence;
        this.date = date;
        this.decision = decision;
        this.state = state;
        this.applicantRole = applicantRole;
    }

    public String getType() {
        requireNonNull(type);
        return type;
    }

    public String getDetails() {
        requireNonNull(details);
        return details;
    }

    public List<IdValue<Document>> getEvidence() {
        requireNonNull(evidence);
        return evidence;
    }

    public String getApplicant() {
        requireNonNull(applicant);
        return applicant;
    }

    public String getDate() {
        requireNonNull(date);
        return date;
    }

    public String getDecision() {
        requireNonNull(decision);
        return decision;
    }

    public String getState() {
        requireNonNull(state);
        return state;
    }

    public String getApplicantRole() {
        requireNonNull(applicantRole);
        return applicantRole;
    }

    public void setDecision(String decision) {
        requireNonNull(decision);
        this.decision = decision;
    }

    public void setDecisionReason(String decisionReason) {
        requireNonNull(decisionReason);
        this.decisionReason = decisionReason;
    }

    public void setDecisionDate(String decisionDate) {
        requireNonNull(decisionDate);
        this.decisionDate = decisionDate;
    }

    public void setDecisionMaker(String decisionMaker) {
        requireNonNull(decisionMaker);
        this.decisionMaker = decisionMaker;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public String getDecisionMaker() {
        return decisionMaker;
    }
}
