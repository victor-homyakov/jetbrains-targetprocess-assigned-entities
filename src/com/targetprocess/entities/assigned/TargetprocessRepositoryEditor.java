package com.targetprocess.entities.assigned;

import com.intellij.openapi.project.Project;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TargetprocessRepositoryEditor extends BaseRepositoryEditor<TargetprocessRepository> {
    private JBCheckBox myUsingBugsCheckbox;
    private JBCheckBox myUsingUserStoriesCheckbox;
    private JBCheckBox myUsingTasksCheckbox;

    public TargetprocessRepositoryEditor(Project project, TargetprocessRepository repository,
        Consumer<? super TargetprocessRepository> changeListener) {
        super(project, repository, changeListener);

        myUsingBugsCheckbox.setSelected(repository.isUsingBugs());
        myUsingUserStoriesCheckbox.setSelected(repository.isUsingUserStories());
        myUsingTasksCheckbox.setSelected(repository.isUsingTasks());

        installListener(myUsingBugsCheckbox);
        installListener(myUsingUserStoriesCheckbox);
        installListener(myUsingTasksCheckbox);
    }

    @Override
    @Nullable
    protected JComponent createCustomPanel() {
        myUsingBugsCheckbox = new JBCheckBox("Use bugs");
        myUsingUserStoriesCheckbox = new JBCheckBox("Use user stories");
        myUsingTasksCheckbox = new JBCheckBox("Use tasks");

        // addComponentToRightColumn() ?
        return FormBuilder
            .createFormBuilder()
            .addComponent(myUsingBugsCheckbox)
            .addComponent(myUsingUserStoriesCheckbox)
            .addComponent(myUsingTasksCheckbox)
            .getPanel();
    }

    @Override
    public void apply() {
        super.apply();

        if (!myUsingBugsCheckbox.isSelected() && !myUsingUserStoriesCheckbox.isSelected() && !myUsingTasksCheckbox.isSelected()) {
            // At least one checkbox should be selected
            myUsingBugsCheckbox.setSelected(true);
        }

        myRepository.setUsingBugs(myUsingBugsCheckbox.isSelected());
        myRepository.setUsingUserStories(myUsingUserStoriesCheckbox.isSelected());
        myRepository.setUsingTasks(myUsingTasksCheckbox.isSelected());
    }
}
