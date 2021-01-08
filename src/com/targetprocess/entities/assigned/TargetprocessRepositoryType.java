package com.targetprocess.entities.assigned;

import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.config.TaskRepositoryEditor;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author khomyackov
 */
public class TargetprocessRepositoryType extends BaseRepositoryType<TargetprocessRepository> {
    @NotNull
    @Override
    public String getName() {
        return "Targetprocess";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return Icons.Targetprocess;
    }

    @NotNull
    @Override
    public TaskRepository createRepository() {
        return new TargetprocessRepository(this);
    }

    @NotNull
    @Override
    public TaskRepositoryEditor createEditor(TargetprocessRepository repository, Project project,
            Consumer<TargetprocessRepository> changeListener) {
        return new TargetprocessRepositoryEditor(project, repository, changeListener);
    }

    @Override
    public Class<TargetprocessRepository> getRepositoryClass() {
        return TargetprocessRepository.class;
    }
}
