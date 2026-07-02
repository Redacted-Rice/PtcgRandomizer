package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import redactedrice.ptcgr.randomizer.actions.Action;

public class SeedOffsetConfigDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Action action;
    private final boolean editable;
    private final JTextField seedOffsetField;

    public SeedOffsetConfigDialog(Window owner, Action action, boolean editable) {
        super(owner, editable ? "Edit Configs" : "Show Configs", ModalityType.APPLICATION_MODAL);
        this.action = action;
        this.editable = editable;

        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        fieldsPanel.add(new JLabel("Seed Offset:"));
        seedOffsetField = new JTextField(String.valueOf(getDisplayedSeedOffset()));
        seedOffsetField.setEditable(editable);
        if (editable) {
            ((AbstractDocument) seedOffsetField.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        }
        fieldsPanel.add(seedOffsetField);
        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (editable) {
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> saveAndClose());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        } else {
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
        }
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private int getDisplayedSeedOffset() {
        // Left side shows module defaults; right side shows the instance value
        return editable ? action.getSeedOffset() : action.getDefaultSeedOffset();
    }

    private void saveAndClose() {
        String text = seedOffsetField.getText().trim();
        if (text.isEmpty() || text.equals("-")) {
            JOptionPane.showMessageDialog(this, "Seed offset must be a number.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            action.setSeedOffset(Integer.parseInt(text));
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seed offset must be a number.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void show(Window owner, Action action, boolean editable) {
        SeedOffsetConfigDialog dialog = new SeedOffsetConfigDialog(owner, action, editable);
        dialog.setVisible(true);
    }

    private static class IntegerDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (isValidInput(fb, offset, string, 0, fb.getDocument().getLength())) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (isValidInput(fb, offset, text, offset, length)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidInput(FilterBypass fb, int offset, String newText, int replaceOffset,
                int replaceLength) throws BadLocationException {
            if (newText == null) {
                return true;
            }
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String updated = current.substring(0, replaceOffset) + newText
                    + current.substring(replaceOffset + replaceLength);
            if (updated.isEmpty()) {
                return true;
            }
            return updated.matches("-?\\d*");
        }
    }
}
