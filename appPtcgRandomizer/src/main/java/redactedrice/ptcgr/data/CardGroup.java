package redactedrice.ptcgr.data;


import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import redactedrice.ptcgr.rules.MoveExclusions;
import redactedrice.ptcgr.constants.CardConstants.CardId;
import redactedrice.ptcgr.constants.CardDataConstants.CardType;
import redactedrice.ptcgr.constants.CardDataConstants.EvolutionStage;
import redactedrice.ptcgr.utils.WarningCollector;

public class CardGroup<T extends Card> {
    private EnumMap<CardId, T> cardsById;

    public CardGroup() {
        cardsById = new EnumMap<>(CardId.class);
    }

    private CardGroup(List<T> list) {
        this();
        list.forEach(c -> cardsById.put(c.id, c));
    }

    public CardGroup<T> copy(Class<? extends T> cardClass) {
        CardGroup<T> copy = new CardGroup<>();
        for (T card : cardsById.values()) {
            copy.add(cardClass.cast(card.copy()));
        }
        return copy;
    }

    public CardGroup<T> recast(Class<? extends T> cardClass) {
        CardGroup<T> recast = new CardGroup<>();
        for (T card : cardsById.values()) {
            recast.add(cardClass.cast(card));
        }
        return recast;
    }

    public CardGroup<Card> upcast() {
        CardGroup<Card> asCard = new CardGroup<>();
        for (T card : cardsById.values()) {
            asCard.add(card);
        }
        return asCard;
    }

    public T first() {
        return cardsById.values().iterator().next();
    }

    public CardGroup<T> withNameIgnoringNumber(String nameNumberIgnored) {
        return new CardGroup<>(cardsById.values().stream()
                .filter(card -> card.name.matchesIgnoringPotentialNumber(nameNumberIgnored))
                .collect(Collectors.toList()));
    }

    // returns null if error encountered or no number was found
    public static <T extends Card> T fromNameSetBasedOnNumber(CardGroup<T> cardsWithSameName,
            String numberOrNameWithNumber) {
        int cardIndex = -1;
        // Assume its a number
        try {
            cardIndex = Integer.parseInt(numberOrNameWithNumber);
        }
        // If not then assume its a name with a number
        catch (NumberFormatException nfe) {
            // All will have the same name so just choose the first
            cardIndex = cardsWithSameName.first().name
                    .getCardNumFromNameIfMatches(numberOrNameWithNumber);
        }

        // If we found an index (0 means no name, negative means failed to match name), return based
        // on the index
        if (cardIndex > 0) {
            // If we found an index, try to get it shifting it to 0 based
            return basedOnIndex(cardsWithSameName, cardIndex - 1);
        }

        return null;
    }

    // Null if index out of bounds
    public static <T extends Card> T basedOnIndex(CardGroup<T> cardsWithSameName, int index) {
        List<T> asList = cardsWithSameName.listOrderedByCardId();

        if (index >= asList.size() || index < 0) {
            return null;
        }

        return asList.get(index);
    }

    public T withId(CardId cardId) {
        return cardsById.get(cardId);
    }

    public CardGroup<T> withIds(Set<CardId> cardIds) {
        CardGroup<T> found = new CardGroup<>();
        for (CardId id : cardIds) {
            found.add(cardsById.get(id));
        }
        return found;
    }

    // TODO later: Move this and some other more logic specific/search
    // functions to a separate class?
    public CardGroup<Card> determineBasicEvolutionOfCard(MonsterCard card) {
        CardGroup<Card> basics = new CardGroup<>();
        if (card.stage == EvolutionStage.BASIC) {
            basics.add(card);
        } else {
            while (card.stage != EvolutionStage.BASIC) {
                basics = withNameIgnoringNumber(card.prevEvoName.toString()).upcast();
                if (basics.count() <= 0) {
                    break;
                }

                // If its not a poke, its probably a trainer like mysterious fossil. Assume
                // this is the "basic" monsters
                if (!card.type.isMonsterCard()) {
                    break;
                }

                // TODO later: Doesn't work with mysterious fossil - we only check the parent not
                // the child
                // is a poke card
                card = (MonsterCard) basics.listOrderedByCardId().get(0);
            }
        }
        return basics;
    }

    public CardGroup<NonMonsterCard> energyCards() {
        return new CardGroup<>(cardsById.values().stream().filter(card -> card.type.isEnergyCard())
                .map(card -> (NonMonsterCard) card).collect(Collectors.toList()));
    }

    public CardGroup<MonsterCard> monsterCards() {
        return new CardGroup<>(cardsById.values().stream().filter(card -> card.type.isMonsterCard())
                .map(card -> (MonsterCard) card).collect(Collectors.toList()));
    }

    public CardGroup<NonMonsterCard> trainerCards() {
        return new CardGroup<>(cardsById.values().stream().filter(card -> card.type.isTrainerCard())
                .map(card -> (NonMonsterCard) card).collect(Collectors.toList()));
    }

    public CardGroup<T> ofCardType(CardType cardType) {
        return new CardGroup<>(cardsById.values().stream()
                .filter(card -> cardType.equals(card.type)).collect(Collectors.toList()));
    }

    public List<Move> allMoves() {
        return allMovesForRandomization(null);
    }

    public List<Move> allMovesForRandomization(MoveExclusions movesToExclude) {
        CardGroup<MonsterCard> pokeCards = monsterCards();
        List<Move> moves = new ArrayList<>();
        for (MonsterCard card : pokeCards.iterable()) {
            for (Move move : card.getAllMovesIncludingEmptyOnes()) {
                if (!move.isEmpty() && (movesToExclude == null
                        || !movesToExclude.isMoveRemovedFromPool(card.id, move))) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    // TODO later: encapsulate safer to prevent editing outside class?
    public Collection<T> iterable() {
        return cardsById.values();
    }

    public Stream<T> stream() {
        return cardsById.values().stream();
    }

    // No sort needed
    public List<T> listOrderedByCardId() {
        // Already sorted by Id
        return new LinkedList<>(cardsById.values());
    }

    public List<T> listCustomSort(Comparator<Card> comparator) {
        List<T> cardsList = listOrderedByCardId();
        Collections.sort(cardsList, comparator);
        return cardsList;
    }

    public void add(T card) {
        cardsById.put(card.id, card);
    }

    public int count() {
        return cardsById.size();
    }

    public boolean isKnownMoveName(String moveName) {
        String trimmed = moveName.trim();
        for (T card : iterable()) {
            if (!(card instanceof MonsterCard monster)) {
                continue;
            }
            for (Move move : monster.getAllMovesIncludingEmptyOnes()) {
                if (!move.isEmpty() && move.name.toString().equalsIgnoreCase(trimmed)) {
                    return true;
                }
            }
        }
        return false;
    }

    public MonsterCard resolveCard(String cardSpecifier, String entryContext,
            WarningCollector warnings) {
        String trimmed = cardSpecifier.trim();
        if (!MonsterCard.isNameWithLevel(trimmed)) {
            String exampleName = trimmed.isEmpty() ? "SomeMonster" : trimmed;
            warnings.addWarning(entryContext + ": monster card \"" + cardSpecifier
                    + "\" must use name and level (e.g. \"" + exampleName + " lvl65\").");
            return null;
        }

        MonsterCard card = findMonsterByNameWithLevel(trimmed);
        if (card == null) {
            warnings.addWarning(
                    entryContext + ": failed to resolve card \"" + cardSpecifier + "\".");
        }
        return card;
    }

    public Move resolveMoveOnCard(MonsterCard hostCard, String moveName, String entryContext,
            WarningCollector warnings) {
        Move moveWithName = hostCard.getMoveWithName(moveName);
        if (moveWithName != null) {
            return moveWithName;
        }

        warnings.addWarning(entryContext + ": failed to find move \"" + moveName + "\" on card \""
                + hostCard.name + "\".");
        return null;
    }

    /**
     * Assignment target slots are 1 based in files. Internal storage is 0 based.
     */
    public int parseMoveSlotId(String slotSpecifier, String entryContext,
            WarningCollector warnings) {
        try {
            int oneBasedSlot = Integer.parseInt(slotSpecifier);
            if (oneBasedSlot >= 1 && oneBasedSlot <= MonsterCard.MAX_NUM_MOVES) {
                return oneBasedSlot - 1;
            }

            warnings.addWarning(entryContext + ": to_move_slot \"" + slotSpecifier
                    + "\" is out of range; use " + 1 + "-" + MonsterCard.MAX_NUM_MOVES + ".");
            return -1;
        } catch (NumberFormatException ignored) {
            warnings.addWarning(entryContext + ": to_move_slot must be a 1-based slot number (1-"
                    + MonsterCard.MAX_NUM_MOVES + ").");
            return -1;
        }
    }

    public boolean cardHasMove(MonsterCard card, String moveName) {
        return card.getMoveWithName(moveName) != null;
    }

    /**
     * Resolved by move name. If optionalFromCard is null the first card in ROM order with that move
     * is used and a warning is logged if more than one card matches.
     */
    public Move resolveMoveByName(String moveName, MonsterCard optionalFromCard,
            String entryContext, WarningCollector warnings) {
        if (optionalFromCard != null) {
            return resolveMoveOnCard(optionalFromCard, moveName, entryContext, warnings);
        }

        MonsterCard firstHost = null;
        for (T card : iterable()) {
            if (!(card instanceof MonsterCard monster)) {
                continue;
            }
            if (monster.getMoveWithName(moveName) == null) {
                continue;
            }
            if (firstHost != null) {
                warnings.addWarning(entryContext + ": move \"" + moveName
                        + "\" was found on multiple cards; using the first match.");
                return firstHost.getMoveWithName(moveName);
            }
            firstHost = monster;
        }

        if (firstHost == null) {
            warnings.addWarning(
                    entryContext + ": failed to find move \"" + moveName + "\" on any card.");
            return null;
        }

        return firstHost.getMoveWithName(moveName);
    }

    private MonsterCard findMonsterByNameWithLevel(String cardSpecifier) {
        MonsterCard.NameWithLevel ref = MonsterCard.parseNameWithLevel(cardSpecifier);
        if (ref == null) {
            return null;
        }
        for (T card : iterable()) {
            if (card instanceof MonsterCard monster && monster.matchesNameWithLevel(ref)) {
                return monster;
            }
        }
        return null;
    }
}
