package redactedrice.ptcgr.randomizer.gui.rules;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import redactedrice.ptcgr.configs.rules.MoveAssignmentConfig;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.rules.Rules;
import redactedrice.randomizer.utils.IssueTracker;

/** Dialog for adding a move assignment from the rules tab. */
final class AddMoveAssignmentDialog extends RulesAddDialog {
    private static final long serialVersionUID = 1L;

    private final Rules rules;
    private final CardGroup<MonsterCard> cards;

    private final JComboBox<String> moveCombo;
    private final JComboBox<String> fromCardCombo;
    private final JComboBox<String> toCardCombo;
    private final JComboBox<String> toSlotCombo;
    private final RulesLinkedMoveCardSelectors linkedFromSelectors;

    private boolean updating;

    private AddMoveAssignmentDialog(Window owner, Rules rules, CardGroup<MonsterCard> cards) {
        super(owner, "Add Move Assignment");
        this.rules = rules;
        this.cards = cards;

        moveCombo = new JComboBox<>();
        fromCardCombo = new JComboBox<>();
        toCardCombo = new JComboBox<>();
        toSlotCombo = new JComboBox<>();
        RulesDialogLayout.prepareCombo(moveCombo);
        RulesDialogLayout.prepareCombo(fromCardCombo);
        RulesDialogLayout.prepareCombo(toCardCombo);
        RulesDialogLayout.prepareCombo(toSlotCombo);
        linkedFromSelectors =
                new RulesLinkedMoveCardSelectors(cards, moveCombo, fromCardCombo, false);

        buildUi();
        linkedFromSelectors.wireListeners();
        wireToCardListener();
        refreshChoices();
    }

    static boolean showDialog(Component parent, Rules rules, CardGroup<MonsterCard> cards) {
        return RulesAddDialog.show(parent, new AddMoveAssignmentDialog(
                parent != null ? javax.swing.SwingUtilities.getWindowAncestor(parent) : null,
                rules, cards));
    }

    private void buildUi() {
        JPanel form = createFormPanel();
        GridBagConstraints labelConstraints = labelConstraints();
        GridBagConstraints fieldConstraints = fieldConstraints();

        int row = 0;
        addFormRow(form, labelConstraints, fieldConstraints, row++, "Move", moveCombo);
        addFormRow(form, labelConstraints, fieldConstraints, row++, "From Card", fromCardCombo);
        addFormRow(form, labelConstraints, fieldConstraints, row++, "To Card", toCardCombo);
        addFormRow(form, labelConstraints, fieldConstraints, row, "To Move Slot", toSlotCombo);

        finishForm(form, this::tryAdd);
    }

    private void wireToCardListener() {
        toCardCombo.addActionListener(event -> {
            if (!updating) {
                onToCardChanged();
            }
        });
    }

    private void refreshChoices() {
        updating = true;
        try {
            String toCard = RulesChoiceData.selectedItem(toCardCombo);
            String slot = RulesChoiceData.selectedItem(toSlotCombo);
            linkedFromSelectors.refreshAll();
            RulesChoiceData.setComboItems(toCardCombo, RulesChoiceData.allCardSpecifiers(cards),
                    toCard);
            refreshSlotChoices(slot);
        } finally {
            updating = false;
        }
    }

    private void onToCardChanged() {
        updating = true;
        try {
            refreshSlotChoices(RulesChoiceData.selectedItem(toSlotCombo));
        } finally {
            updating = false;
        }
    }

    private void refreshSlotChoices(String preferredSlot) {
        MonsterCard targetCard =
                RulesChoiceData.resolveCard(cards, RulesChoiceData.selectedItem(toCardCombo));
        RulesChoiceData.setComboItems(toSlotCombo, RulesChoiceData.moveSlotLabels(targetCard),
                preferredSlot);
    }

    private void tryAdd() {
        String move = RulesChoiceData.requireSelectedItem(moveCombo);
        if (move == null || move.isBlank()) {
            showIssueTrackerWarningOr("Choose a move.");
            return;
        }

        String toCard = RulesChoiceData.requireSelectedItem(toCardCombo);
        if (toCard == null || toCard.isBlank()) {
            showIssueTrackerWarningOr("Choose a target card.");
            return;
        }

        String toSlot = RulesChoiceData.requireSelectedItem(toSlotCombo);
        if (toSlot == null || toSlot.isBlank()) {
            showIssueTrackerWarningOr("Choose a target move slot.");
            return;
        }

        String fromCard = RulesChoiceData.requireSelectedItem(fromCardCombo);
        if (RulesChoiceData.moveExistsOnMultipleCards(cards, move)) {
            if (fromCard == null || fromCard.isBlank()) {
                showIssueTrackerWarningOr(
                        "That move exists on multiple cards. Choose a from card.");
                return;
            }
        } else {
            MonsterCard soleHost = RulesChoiceData.soleCardWithMove(cards, move);
            if (soleHost != null) {
                fromCard = soleHost.toNameWithLevelSpecifier();
            }
        }

        IssueTracker.clear();
        MoveAssignmentConfig config = new MoveAssignmentConfig(toCard, toSlot, move, fromCard);
        MoveAssignment assignment = config.toMoveAssignment(cards, RulesPanel.USER_ADDED_SOURCE,
                "rules tab");
        if (assignment == null) {
            showIssueTrackerWarningOr("Could not add that assignment. Check the fields.");
            return;
        }

        if (!rules.addMoveAssignment(assignment, cards)) {
            showIssueTrackerWarningOr("That slot already has a different assignment.");
            return;
        }
        confirmAndClose();
    }
}
