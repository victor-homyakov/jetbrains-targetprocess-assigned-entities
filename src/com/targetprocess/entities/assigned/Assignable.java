package com.targetprocess.entities.assigned;

import com.google.gson.annotations.SerializedName;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

public class Assignable extends Task {

    public static final String FIELDS = "id,name,description,entityType:entityType.name";

    public static String serverUrl;

    private final long id;
    @SerializedName("name")
    private final String summary;
    private final String description;
    private final String entityType;

    public Assignable(long id, String summary, String description, String entityType) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.entityType = entityType;
    }

    @NotNull
    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @NotNull
    @Override
    public String getPresentableId() {
        return TaskIdConverter.getPresentableId(id, entityType);
    }

    @NotNull
    @Override
    public String getNumber() {
        return getId();
    }

    @NotNull
    @Override
    public String getSummary() {
        return summary;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @NotNull
    @Override
    public Comment[] getComments() {
        return new Comment[0]; //TODO comments?
    }

    @NotNull
    @Override
    public Icon getIcon() {
        switch (getType()) {
            case BUG:
                return Icons.Bug;
            case FEATURE:
                // AllIcons.Nodes.Favorite;
                return Icons.Feature;
            default:
                // AllIcons.FileTypes.Any_type;
                // AllIcons.FileTypes.Unknown;
                return isIssue() ? Icons.Other : Icons.Unknown;
        }
    }

    @NotNull
    @Override
    public TaskType getType() {
        switch (entityType) {
            case "Bug":
                return TaskType.BUG;
            case "UserStory":
                return TaskType.FEATURE;
            default:
                return TaskType.OTHER;
        }
    }

    @Nullable
    @Override
    public Date getUpdated() {
        return null; //TODO modifyDate
    }

    @Nullable
    @Override
    public Date getCreated() {
        return null; //TODO createDate
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isIssue() {
        return true;
    }

    @Nullable
    @Override
    public String getIssueUrl() {
        return serverUrl + "/entity/" + getId();
    }

}
