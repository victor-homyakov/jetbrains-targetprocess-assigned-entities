package com.targetprocess.entities.assigned;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author khomyackov
 */
public class TargetprocessIcon {

    public static final Icon Targetprocess = load();

    private static Icon load() {
        return IconLoader.getIcon("/icons/targetprocess.png", TargetprocessIcon.class);
    }

}
