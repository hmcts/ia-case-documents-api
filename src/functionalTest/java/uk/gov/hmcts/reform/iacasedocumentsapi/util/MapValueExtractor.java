package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public final class MapValueExtractor {

    private MapValueExtractor() {
        // noop
    }

    public static <T> T extract(Map<String, Object> map, String path, Boolean featureFlag) {

        if (path.equals("expectation")) {
            return (T) map.get(path);
        }

        if (!path.contains(".")) {
            return (T) map.get(path);
        }
        Map<String, Object> currentMap = map;

        String[] pathParts = path.split("\\.");

        for (int i = 0; i < pathParts.length - 1; i++) {

            Object value = currentMap.get(pathParts[i]);

            if (!(value instanceof Map)) {
                return null;
            }

            Map<String, Object> expectationMap = (Map<String, Object>) value;

            if (!featureFlag) {

                if (expectationMap.containsKey("confirmation")) {
                    Map<String, Object> confirmation = (Map<String, Object>) expectationMap.get("confirmation");
                    Map<String, Object> updateConfirmation = new HashMap<>(confirmation);

                    if (confirmation.containsKey("body")) {

                        String body = (String) confirmation.get("body");

                        if (body.contains("contains([")) {

                            if (body.contains("_CASE_OFFICER")) {
                                final String sPattern = "(?i)\\b\\w*" + Pattern.quote("_CASE_OFFICER") + "\\w*\\b";
                                Pattern pattern = Pattern.compile(sPattern);
                                Matcher matcher = pattern.matcher(body);
                                String transformedBody = "";

                                while (matcher.find()) {
                                    transformedBody = body.replace(matcher.group(), "");
                                }

                                confirmation.remove("body");
                                if (!transformedBody.isEmpty()) {
                                    updateConfirmation.put("body", transformedBody);
                                }

                                ((Map<String, Object>) value).remove("confirmation");
                                ((Map<String, Object>) value).put("confirmation", updateConfirmation);
                            }
                        }
                    }
                }

                if (expectationMap.containsKey("caseData")) {
                    Map<String, Object> caseData = (Map<String, Object>) expectationMap.get("caseData");
                    Map<String, Object> updateCaseData = new HashMap<>(caseData);

                    if (updateCaseData.containsKey("notifications")) {
                        List<Map<String, Object>> caseDataNotifications = (List<Map<String, Object>>) updateCaseData.get(
                            "notifications");
                        if (caseDataNotifications == null) {
                            caseDataNotifications = new ArrayList<>();
                        }
                        List<Map<String, Object>> updatedCaseDataNotifications = new ArrayList<>(caseDataNotifications);

                        for (Map<String, Object> notification : caseDataNotifications) {
                            String notificationValue = (String) notification.get("reference");

                            if (notificationValue.contains("_CASE_OFFICER")) {
                                updatedCaseDataNotifications.remove(notification);
                            }
                        }
                        ((Map<String, Object>) value).remove("notifications");
                        if (!updatedCaseDataNotifications.isEmpty()) {
                            ((Map<String, Object>) value).put("notifications", updatedCaseDataNotifications);
                        }
                    }

                    if (updateCaseData.containsKey("replacements")) {
                        Map<String, Object> replacement = (Map<String, Object>) updateCaseData.get("replacements");
                        Map<String, Object> updatedReplacement = new HashMap<>(replacement);

                        if (replacement.containsKey("notificationsSent")) {
                            List<Map<String, Object>> notificationsSent = (List<Map<String, Object>>) updatedReplacement.get(
                                "notificationsSent");
                            if (notificationsSent == null) {
                                notificationsSent = new ArrayList<>();
                            }
                            List<Map<String, Object>> updatedNotificationsSent = new ArrayList<>(notificationsSent);

                            for (Map<String, Object> notificationSent : notificationsSent) {
                                String notificationValue = (String) notificationSent.get("id");

                                if (notificationValue.contains("_CASE_OFFICER")) {
                                    updatedNotificationsSent.remove(notificationSent);
                                }
                            }

                            updatedReplacement.remove("notificationsSent");
                            if (!updatedNotificationsSent.isEmpty()) {
                                updatedReplacement.put("notificationsSent", updatedNotificationsSent);
                            }

                            updateCaseData.remove("replacements");
                            updateCaseData.put("replacements", updatedReplacement);

                            ((Map<String, Object>) value).remove("caseData");
                            ((Map<String, Object>) value).put("caseData", updateCaseData);
                        }
                    }
                }

                if (expectationMap.containsKey("notifications")) {
                    List<Map<String, Object>> notifications = (List<Map<String, Object>>) expectationMap.get(
                        "notifications");
                    if (notifications == null) {
                        notifications = new ArrayList<>();
                    }
                    List<Map<String, Object>> updatedNotifications = new ArrayList<>(notifications);

                    for (Map<String, Object> notification : notifications) {
                        String notificationValue = (String) notification.get("reference");

                        if (notificationValue.contains("_CASE_OFFICER")) {
                            updatedNotifications.remove(notification);
                        }
                    }
                    ((Map<String, Object>) value).remove("notifications");
                    if (!updatedNotifications.isEmpty()) {
                        ((Map<String, Object>) value).put("notifications", updatedNotifications);
                    }
                }
            }
            currentMap = (Map<String, Object>) value;
        }

        return (T) currentMap.get(pathParts[pathParts.length - 1]);
    }

    public static <T> T extract(Map<String, Object> map, String path) {
        return extract(map, path, false);
    }

    public static <T> T extractOrDefault(Map<String, Object> map, String path, T defaultValue, Boolean featureFlag) {

        T value = extract(map, path, featureFlag);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T extractOrDefault(Map<String, Object> map, String path, T defaultValue) {

        T value = extract(map, path);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T extractOrThrow(Map<String, Object> map, String path) {

        T value = extract(map, path, false);

        if (value == null) {
            throw new RuntimeException("Missing value for path: " + path);
        }

        return value;
    }
}
