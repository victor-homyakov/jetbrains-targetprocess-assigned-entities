package com.targetprocess.entities.assigned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskIdConverter {
    private static final Pattern TASK_NAME_PATTERN = Pattern.compile("(?:Bug|US|Task|Entity)#(\\d+)");
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)");

    public static String getPresentableId(long id, String entityType) {
        switch (entityType) {
            case "Bug":
                return "Bug#" + id;
            case "UserStory":
                return "US#" + id;
            case "Task":
                return "Task#" + id;
            default:
                return "Entity#" + id;
        }
    }

    public static String extractId(String taskName) {
        Pattern pattern = taskName.contains("#") ? TASK_NAME_PATTERN : ID_PATTERN;
        Matcher matcher = pattern.matcher(taskName);
        return matcher.find() ? matcher.group(1) : null;
    }
}
