package com.targetprocess.entities.assigned;

import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.BaseRepositoryType;
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

    @Override
    public Class<TargetprocessRepository> getRepositoryClass() {
        return TargetprocessRepository.class;
    }

}
