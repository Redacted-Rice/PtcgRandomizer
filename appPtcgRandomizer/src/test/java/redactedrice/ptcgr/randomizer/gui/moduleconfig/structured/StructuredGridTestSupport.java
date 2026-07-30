package redactedrice.ptcgr.randomizer.gui.moduleconfig.structured;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

public final class StructuredGridTestSupport {
    private StructuredGridTestSupport() {}

    public static JButton findAddButton(Component root) {
        List<JButton> addButtons = findAddButtons(root);
        if (addButtons.isEmpty()) {
            throw new AssertionError("No add button found");
        }
        return addButtons.get(addButtons.size() - 1);
    }

    public static List<JButton> findAddButtons(Component root) {
        List<JButton> addButtons = new ArrayList<>();
        for (JButton button : findComponents(root, JButton.class)) {
            if ("+ Add".equals(button.getText())) {
                addButtons.add(button);
            }
        }
        return addButtons;
    }

    public static List<JButton> findRemoveButtons(Component root) {
        List<JButton> removeButtons = new ArrayList<>();
        for (JButton button : findComponents(root, JButton.class)) {
            if ("\u00D7".equals(button.getText())) {
                removeButtons.add(button);
            }
        }
        return removeButtons;
    }

    public static boolean isFramedRemoveButton(JButton button) {
        return !(button.getParent() instanceof StructuredGridPanel);
    }

    public static <T> List<T> findComponents(Component root, Class<T> type) {
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

    public static void layoutFully(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                layoutFully(child);
            }
        }
    }

    public static GridBagConstraints gridBagConstraintsOf(Component component) {
        Component current = component;
        while (current != null) {
            Container parent = current.getParent();
            if (parent instanceof StructuredGridPanel panel
                    && panel.getLayout() instanceof GridBagLayout layout) {
                return layout.getConstraints(current);
            }
            current = parent;
        }
        throw new AssertionError("No StructuredGridPanel ancestor found for: " + component);
    }

    public static boolean isHorizontalSeparator(Component component) {
        if (!(component.getParent() instanceof StructuredGridPanel)) {
            return false;
        }
        GridBagConstraints constraints = gridBagConstraintsOf(component);
        return constraints.fill == GridBagConstraints.HORIZONTAL && constraints.gridwidth > 1
                && component.getPreferredSize().height == 1;
    }

    public static long countHorizontalSeparators(Component root) {
        long count = 0;
        if (root instanceof StructuredGridPanel panel) {
            for (Component child : panel.getComponents()) {
                if (isHorizontalSeparator(child)) {
                    count++;
                }
            }
        }
        return count;
    }
}
