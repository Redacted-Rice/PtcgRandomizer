package redactedrice.ptcgr.randomizer.gui.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComboBox;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;

/** Move and card pick lists for the rules tab add dialogs. */
final class RulesChoiceData {
    static final String ANY_CARD_LABEL = "(any card)";

    private static final Comparator<String> NAME_ORDER = String.CASE_INSENSITIVE_ORDER;

    private RulesChoiceData() {
    }

    static List<String> distinctMoveNames(CardGroup<MonsterCard> cards) {
        Set<String> names = new TreeSet<>(NAME_ORDER);
        if (cards == null) {
            return List.of();
        }
        for (MonsterCard card : cards.iterable()) {
            for (Move move : card.getAllMoves(false)) {
                if (!move.isEmpty()) {
                    names.add(move.name.toString());
                }
            }
        }
        return List.copyOf(names);
    }

    static List<String> cardSpecifiersForMove(CardGroup<MonsterCard> cards, String moveName) {
        Set<String> specifiers = new TreeSet<>(NAME_ORDER);
        if (cards == null) {
            return List.of();
        }
        for (MonsterCard card : cards.iterable()) {
            if (moveName == null || moveName.isBlank() || cards.cardHasMove(card, moveName)) {
                specifiers.add(card.toNameWithLevelSpecifier());
            }
        }
        return List.copyOf(specifiers);
    }

    static List<String> moveNamesOnCard(MonsterCard card) {
        Set<String> names = new TreeSet<>(NAME_ORDER);
        if (card == null) {
            return List.of();
        }
        for (Move move : card.getAllMoves(false)) {
            if (!move.isEmpty()) {
                names.add(move.name.toString());
            }
        }
        return List.copyOf(names);
    }

    static List<String> allCardSpecifiers(CardGroup<MonsterCard> cards) {
        return cardSpecifiersForMove(cards, null);
    }

    static List<String> moveSlotLabels(MonsterCard card) {
        int slotCount = card != null ? Math.max(card.getNumMoves(), 1) : MonsterCard.MAX_NUM_MOVES;
        List<String> slots = new ArrayList<>(slotCount);
        for (int slot = 1; slot <= slotCount; slot++) {
            slots.add(String.valueOf(slot));
        }
        return slots;
    }

    static boolean moveExistsOnMultipleCards(CardGroup<MonsterCard> cards, String moveName) {
        if (cards == null || moveName == null || moveName.isBlank()) {
            return false;
        }
        int matchCount = 0;
        for (MonsterCard card : cards.iterable()) {
            if (cards.cardHasMove(card, moveName)) {
                matchCount++;
                if (matchCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    static MonsterCard soleCardWithMove(CardGroup<MonsterCard> cards, String moveName) {
        if (cards == null || moveName == null || moveName.isBlank()) {
            return null;
        }
        MonsterCard match = null;
        for (MonsterCard card : cards.iterable()) {
            if (!cards.cardHasMove(card, moveName)) {
                continue;
            }
            if (match != null) {
                return null;
            }
            match = card;
        }
        return match;
    }

    static MonsterCard resolveCard(CardGroup<MonsterCard> cards, String specifier) {
        if (cards == null || specifier == null || specifier.isBlank()
                || ANY_CARD_LABEL.equals(specifier)) {
            return null;
        }
        return cards.resolveCard(specifier, "rules tab");
    }

    static void setComboItems(JComboBox<String> combo, List<String> items, String preferredSelection) {
        String current = preferredSelection != null ? preferredSelection : selectedItem(combo);
        combo.removeAllItems();
        for (String item : items) {
            combo.addItem(item);
        }
        if (current != null && items.contains(current)) {
            combo.setSelectedItem(current);
        } else if (!items.isEmpty()) {
            combo.setSelectedIndex(0);
        }
    }

    static String selectedItem(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected != null) {
            return selected.toString();
        }
        int index = combo.getSelectedIndex();
        if (index >= 0 && index < combo.getItemCount()) {
            return combo.getItemAt(index);
        }
        return null;
    }

    static String requireSelectedItem(JComboBox<String> combo) {
        String selected = selectedItem(combo);
        if (selected != null && !selected.isBlank()) {
            return selected;
        }
        if (combo.getItemCount() > 0) {
            return combo.getItemAt(0);
        }
        return null;
    }
}
