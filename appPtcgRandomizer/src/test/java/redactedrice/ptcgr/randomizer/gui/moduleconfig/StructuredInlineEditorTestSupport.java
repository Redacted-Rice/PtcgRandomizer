package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

final class StructuredInlineEditorTestSupport {
    private StructuredInlineEditorTestSupport() {}

    static JButton findAddButton(Component root) {
        for (JButton button : findComponents(root, JButton.class)) {
            if ("+ Add".equals(button.getText())) {
                return button;
            }
        }
        throw new AssertionError("No add button found");
    }

    static List<JButton> findRemoveButtons(Component root) {
        List<JButton> removeButtons = new ArrayList<>();
        for (JButton button : findComponents(root, JButton.class)) {
            if ("\u00D7".equals(button.getText())) {
                removeButtons.add(button);
            }
        }
        return removeButtons;
    }

    static List<JButton> findEntryRemoveButtons(Component root) {
        JPanel rowsPanel = findRowsPanel(root);
        List<JButton> removeButtons = new ArrayList<>();
        for (Component child : rowsPanel.getComponents()) {
            if (child instanceof JPanel rowPanel && !isAddRowPanel(rowPanel)) {
                JButton removeButton = findOuterRemoveButton(rowPanel);
                if (removeButton != null) {
                    removeButtons.add(removeButton);
                }
            }
        }
        return removeButtons;
    }

    static JButton findFirstOuterRemoveButton(Component root) {
        JPanel rowsPanel = findRowsPanel(root);
        for (Component child : rowsPanel.getComponents()) {
            if (!(child instanceof JPanel rowPanel) || isAddRowPanel(rowPanel)) {
                continue;
            }
            JButton removeButton = findOuterRemoveButton(rowPanel);
            if (removeButton != null) {
                return removeButton;
            }
        }
        throw new AssertionError("No outer remove button found");
    }

    static JPanel findRowsPanel(Component root) {
        if (root instanceof JPanel panel && panel.getLayout() instanceof BoxLayout) {
            return panel;
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                JPanel rowsPanel = findRowsPanel(child);
                if (rowsPanel != null) {
                    return rowsPanel;
                }
            }
        }
        throw new AssertionError("No rows panel found");
    }

    static <T> List<T> findComponents(Component root, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(root)) {
            found.add(type.cast(root));
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                found.addAll(findComponents(child, type));
            }
        }
        return found;
    }

    private static boolean isAddRowPanel(JPanel panel) {
        for (Component child : panel.getComponents()) {
            if (child instanceof JButton button && "+ Add".equals(button.getText())) {
                return true;
            }
        }
        return false;
    }

    private static JButton findOuterRemoveButton(JPanel rowPanel) {
        for (Component child : rowPanel.getComponents()) {
            if (child instanceof JButton button && "\u00D7".equals(button.getText())) {
                return button;
            }
        }
        return null;
    }
}
