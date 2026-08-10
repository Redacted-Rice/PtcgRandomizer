package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import redactedrice.ptcgr.configs.rules.MoveExclusionConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveExclusion;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.randomizer.utils.IssueTracker;

/** Dialog for adding a move exclusion from the rules tab. */
final class AddMoveExclusionDialog extends RulesAddDialog {
    private static final long serialVersionUID = 1L;
    private static final String[] YES_NO_CHOICES = { "No", "Yes" };

    private final Rules rules;
    private final CardGroup<MonsterCard> cards;

    private final JComboBox<String> moveCombo;
    private final JComboBox<String> cardCombo;
    private final JTextField moveField;
    private final JTextField cardField;
    private final JComboBox<String> removeFromPoolCombo;
    private final JComboBox<String> generateAssignmentsCombo;
    private final RulesLinkedMoveCardSelectors linkedSelectors;

    private AddMoveExclusionDialog(Window owner, Rules rules, CardGroup<MonsterCard> cards) {
        super(owner, "Add Move Exclusion");
        this.rules = rules;
        this.cards = cards;

        moveCombo = new JComboBox<>();
        cardCombo = new JComboBox<>();
        moveField = new JTextField(24);
        cardField = new JTextField(24);
        removeFromPoolCombo = new JComboBox<>(YES_NO_CHOICES);
        generateAssignmentsCombo = new JComboBox<>(YES_NO_CHOICES);
        if (cards != null) {
            RulesDialogLayout.prepareCombo(moveCombo);
            RulesDialogLayout.prepareCombo(cardCombo);
            linkedSelectors = new RulesLinkedMoveCardSelectors(cards, moveCombo, cardCombo, true);
        } else {
            RulesDialogLayout.widenField(moveField);
            RulesDialogLayout.widenField(cardField);
            linkedSelectors = null;
        }

        buildUi();
        if (linkedSelectors != null) {
            linkedSelectors.wireListeners();
            linkedSelectors.refreshAll();
        }
    }

    static boolean showDialog(Component parent, Rules rules, CardGroup<MonsterCard> cards) {
        return RulesAddDialog.show(parent, new AddMoveExclusionDialog(
                parent != null ? javax.swing.SwingUtilities.getWindowAncestor(parent) : null,
                rules, cards));
    }

    private void buildUi() {
        JPanel form = createFormPanel();
        GridBagConstraints labelConstraints = labelConstraints();
        GridBagConstraints fieldConstraints = fieldConstraints();

        int row = 0;
        addFormRow(form, labelConstraints, fieldConstraints, row++, "Move",
                cards != null ? moveCombo : moveField);
        addFormRow(form, labelConstraints, fieldConstraints, row++, "Card",
                cards != null ? cardCombo : cardField);
        addFormRow(form, labelConstraints, fieldConstraints, row++, "Remove from Pool",
                removeFromPoolCombo);
        addFormRow(form, labelConstraints, fieldConstraints, row, "Generate Assignments",
                generateAssignmentsCombo);

        finishForm(form, this::tryAdd);
    }

    private void tryAdd() {
        String move = cards != null ? RulesChoiceData.requireSelectedItem(moveCombo)
                : moveField.getText().trim();
        if (move == null || move.isBlank()) {
            showIssueTrackerWarningOr("Choose a move.");
            return;
        }

        String cardSpecifier;
        if (cards != null) {
            String selectedCard = RulesChoiceData.requireSelectedItem(cardCombo);
            cardSpecifier = RulesChoiceData.ANY_CARD_LABEL.equals(selectedCard) ? ""
                    : selectedCard;
        } else {
            cardSpecifier = cardField.getText().trim();
        }

        boolean removeFromPool = "Yes".equals(removeFromPoolCombo.getSelectedItem());
        boolean generateAssignments = "Yes".equals(generateAssignmentsCombo.getSelectedItem());

        IssueTracker.clear();
        MoveExclusionConfig config = new MoveExclusionConfig(RulesPanel.USER_ADDED_SOURCE,
                removeFromPool, generateAssignments, move, cardSpecifier);
        MoveExclusion exclusion = config.toMoveExclusion(cards, RulesPanel.USER_ADDED_SOURCE,
                "rules tab");
        if (exclusion == null) {
            showIssueTrackerWarningOr("Could not add that exclusion. Check the move and card.");
            return;
        }

        if (!rules.addMoveExclusion(exclusion, cards)) {
            showIssueTrackerWarningOr("Could not add that exclusion.");
            return;
        }
        confirmAndClose();
    }
}
