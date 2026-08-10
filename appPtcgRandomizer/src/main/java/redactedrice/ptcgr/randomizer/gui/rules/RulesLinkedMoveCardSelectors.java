package redactedrice.ptcgr.randomizer.gui.rules;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

/** Keeps move and card combos in sync for rules add dialogs. */
final class RulesLinkedMoveCardSelectors {
    private final CardGroup<MonsterCard> cards;
    private final JComboBox<String> moveCombo;
    private final JComboBox<String> cardCombo;
    private final boolean includeAnyCard;
    private boolean updating;

    RulesLinkedMoveCardSelectors(CardGroup<MonsterCard> cards, JComboBox<String> moveCombo,
            JComboBox<String> cardCombo, boolean includeAnyCard) {
        this.cards = cards;
        this.moveCombo = moveCombo;
        this.cardCombo = cardCombo;
        this.includeAnyCard = includeAnyCard;
    }

    void wireListeners() {
        moveCombo.addActionListener(event -> {
            if (!updating) {
                onMoveChanged();
            }
        });
        cardCombo.addActionListener(event -> {
            if (!updating) {
                onCardChanged();
            }
        });
    }

    void refreshAll() {
        updating = true;
        try {
            String card = RulesChoiceData.selectedItem(cardCombo);
            RulesChoiceData.setComboItems(moveCombo, RulesChoiceData.distinctMoveNames(cards),
                    RulesChoiceData.selectedItem(moveCombo));
            refreshCardChoices(card);
        } finally {
            updating = false;
        }
    }

    void onMoveChanged() {
        updating = true;
        try {
            refreshCardChoices(RulesChoiceData.selectedItem(cardCombo));
            MonsterCard soleHost = RulesChoiceData.soleCardWithMove(cards,
                    RulesChoiceData.selectedItem(moveCombo));
            if (soleHost != null && !includeAnyCard) {
                cardCombo.setSelectedItem(soleHost.toNameWithLevelSpecifier());
            }
        } finally {
            updating = false;
        }
    }

    void onCardChanged() {
        updating = true;
        try {
            MonsterCard monsterCard =
                    RulesChoiceData.resolveCard(cards, RulesChoiceData.selectedItem(cardCombo));
            String move = RulesChoiceData.selectedItem(moveCombo);
            List<String> moveItems = monsterCard != null ? RulesChoiceData.moveNamesOnCard(monsterCard)
                    : RulesChoiceData.distinctMoveNames(cards);
            RulesChoiceData.setComboItems(moveCombo, moveItems, move);
        } finally {
            updating = false;
        }
    }

    private void refreshCardChoices(String preferredCard) {
        String move = RulesChoiceData.selectedItem(moveCombo);
        List<String> cardItems = new ArrayList<>();
        if (includeAnyCard) {
            cardItems.add(RulesChoiceData.ANY_CARD_LABEL);
        }
        cardItems.addAll(RulesChoiceData.cardSpecifiersForMove(cards, move));
        RulesChoiceData.setComboItems(cardCombo, cardItems, preferredCard);
    }
}
