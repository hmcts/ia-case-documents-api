package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;


import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SUBMIT_NOTIFICATION_STATUS;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.BailNotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;

@Slf4j
@Configuration
public class BailNotificationHandlerConfiguration {
    @Bean
    public PreSubmitCallbackHandler<BailCase> submitApplicationWithLegalRepNotificationHandler(
        @Qualifier("submitApplicationNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                                 && (callback.getEvent() == Event.SUBMIT_APPLICATION
                                                    || callback.getEvent() == Event.MAKE_NEW_APPLICATION));
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return ((callback.getEvent() == Event.SUBMIT_APPLICATION
                                || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
                                && isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> submitApplicationWithoutLegalRepNotificationHandler(
        @Qualifier("submitApplicationWithoutLegalRepNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && (callback.getEvent() == Event.SUBMIT_APPLICATION
                                                || callback.getEvent() == Event.MAKE_NEW_APPLICATION));
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return ((callback.getEvent() == Event.SUBMIT_APPLICATION
                            || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
                            && !isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSummaryNotificationHandler(
            @Qualifier("uploadSummaryNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY
                                && isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSummaryWithoutLrNotificationHandler(
            @Qualifier("uploadSummaryWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY
                                && !isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }

                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSignedDecisionNoticeNotificationHandler(
        @Qualifier("uploadSignedDecisionNoticeNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return ((callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE)
                            && isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadSignedDecisionNoticeWithoutLrNotificationHandler(
        @Qualifier("uploadSignedDecisionNoticeWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return ((callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE)
                            && !isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> endApplicationNotificationHandler(
        @Qualifier("endApplicationNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.END_APPLICATION);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return ((callback.getEvent() == Event.END_APPLICATION)
                            && isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> endApplicationNotificationWithoutLrHandler(
        @Qualifier("endApplicationWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.END_APPLICATION);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return ((callback.getEvent() == Event.END_APPLICATION)
                            && !isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadDocumentNotificationHandler(
            @Qualifier("uploadDocumentNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_DOCUMENTS);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.UPLOAD_DOCUMENTS
                                && isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> uploadDocumentWithoutLrNotificationHandler(
            @Qualifier("uploadDocumentWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.UPLOAD_DOCUMENTS);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.UPLOAD_DOCUMENTS
                                && !isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }

                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> sendBailDirectionNotificationHandler(
        @Qualifier("sendBailDirectionNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                     && callback.getEvent() == Event.SEND_BAIL_DIRECTION,
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> editBailDocumentsNotificationHandler(
        @Qualifier("editBailDocumentsNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS
                            && isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> editBailDocumentsWithoutLrNotificationHandler(
        @Qualifier("editBailDocumentsWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS
                            && !isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> changeBailDirectionDueDateNotificationHandler(
            @Qualifier("changeBailDirectionDueDateNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
                                && isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }
                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> changeBailDirectionDueDateWithoutLrNotificationHandler(
            @Qualifier("changeBailDirectionDueDateWithoutLrNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
                (callbackStage, callback) -> {
                    boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
                    if (isAllowedBailCase) {
                        BailCase bailCase = callback.getCaseDetails().getCaseData();
                        return (callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
                                && !isLegallyRepresented(bailCase));
                    } else {
                        return false;
                    }

                },
                bailNotificationGenerators,
                getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> editApplicationAfterSubmitWithLegalRepNotificationHandler(
        @Qualifier("editApplicationAfterSubmitNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                            && isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<BailCase> editApplicationAfterSubmitWithoutLegalRepNotificationHandler(
        @Qualifier("editApplicationAfterSubmitWithoutLegalRepNotificationGenerator") List<BailNotificationGenerator> bailNotificationGenerators
    ) {
        return new BailNotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedBailCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                             && callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
                if (isAllowedBailCase) {
                    BailCase bailCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                            && !isLegallyRepresented(bailCase));
                } else {
                    return false;
                }
            },
            bailNotificationGenerators,
            getErrorHandler()
        );
    }

    private ErrorHandler<BailCase> getErrorHandler() {
        ErrorHandler<BailCase> errorHandler = (callback, e) -> {
            callback
                .getCaseDetails()
                .getCaseData()
                .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
        };
        return errorHandler;
    }

    private boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }

}


