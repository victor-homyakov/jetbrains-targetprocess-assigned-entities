package com.targetprocess.entities.assigned;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class Icons {
    public static final Icon Targetprocess = load("/icons/targetprocess.png");
    public static final Icon Bug = load("/icons/bug.svg");
    public static final Icon Feature = load("/icons/feature.svg");
    public static final Icon Other = load("/icons/other.svg");
    public static final Icon Unknown = load("/icons/unknown.svg");

    @NotNull
    private static Icon load(String path) {
        return IconManager.getInstance().getIcon(path, Icons.class);
    }
}
