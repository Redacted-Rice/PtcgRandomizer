package redactedrice.ptcgr.data;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.InstructionParser;
import redactedrice.compiler.RawBytePacker;
import redactedrice.rompacker.Blocks;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.CardDataConstants.*;
import redactedrice.ptcgr.data.romtexts.CardName;
import redactedrice.ptcgr.data.romtexts.MonsterCategory;
import redactedrice.ptcgr.data.romtexts.PokeDescription;
import redactedrice.ptcgr.rom.Cards;
import redactedrice.ptcgr.rom.Texts;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.randomizer.utils.Logger;

public class MonsterCard extends Card {
    public static final int TOTAL_SIZE_IN_BYTES = 65;
    public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
    public static final int MAX_NUM_MOVES = 2;
    private static final Pattern NAME_WITH_LEVEL_PATTERN =
            Pattern.compile("(.+?)\\s+lvl\\s*(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);

    public record NameWithLevel(String name, int level) {
    }

    // TODO Make some of these private to ensure safe values (e.g. multiple of 10 for hp?)
    private byte hp;
    public EvolutionStage stage; // TODO later: Encaspsulate?
    public CardName prevEvoName; // TODO later: Encaspsulate?

    private Move[] moves;
    // Number of active move slots (0..MAX_NUM_MOVES)
    private int numMoves;

    public byte retreatCost; // TODO later: max allowed?
    public WeaknessResistanceType weakness; // Allows multiple
    public WeaknessResistanceType resistance; // Allows multiple
    public MonsterCategory monsterCategory; // TODO later: Investigate? Any gameplay impact?
    public byte dexNumber;
    public byte unknownByte1; // TODO later: Always 0?
    public byte level; // TODO later: Investigate No gameplay impact?
    public byte lengthFt; // TODO later: Investigate No gameplay impact?
    public byte lengthIn; // TODO later: Investigate No gameplay impact?
    public short weight; // TODO later: Investigate No gameplay impact?
    public PokeDescription description;
    public byte unknownByte2; // TODO later: At least somewhat tracks with evo stage in asm files -
                              // 19 for first stage, 16 for second stage, 0 for final stage?

    public MonsterCard() {
        super();

        prevEvoName = new CardName(true); // Pokename
        moves = new Move[MAX_NUM_MOVES];
        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            moves[moveIndex] = new Move(this, moveIndex);
        }
        numMoves = 0;
        monsterCategory = new MonsterCategory();
        description = new PokeDescription();
    }

    @Override
    protected CardName createCardName() {
        return new CardName(true); // a pokename
    }

    @Override
    public MonsterCard copy() {
        MonsterCard copy = new MonsterCard();
        copy.copyMonsterCardFields(this);
        return copy;
    }

    protected void copyMonsterCardFields(MonsterCard toCopy) {
        copyCardFields(toCopy);

        setHp(toCopy.getHp());
        stage = toCopy.stage;
        prevEvoName = new CardName(toCopy.prevEvoName);
        // Set the moves. THis will copy and retarget the moves metadata
        setMoves(toCopy.getAllMoves(true));
        retreatCost = toCopy.retreatCost;
        weakness = toCopy.weakness;
        resistance = toCopy.resistance;
        monsterCategory = new MonsterCategory(toCopy.monsterCategory);
        dexNumber = toCopy.dexNumber;
        unknownByte1 = toCopy.unknownByte1;
        level = toCopy.level;
        lengthFt = toCopy.lengthFt;
        lengthIn = toCopy.lengthIn;
        weight = toCopy.weight;
        description = new PokeDescription(toCopy.description);
        unknownByte2 = toCopy.unknownByte2;
    }

    public int getLevel() {
        return level & 0xFF;
    }

    public static boolean isNameWithLevel(String cardSpecifier) {
        return parseNameWithLevel(cardSpecifier) != null;
    }

    public static NameWithLevel parseNameWithLevel(String cardSpecifier) {
        Matcher matcher = NAME_WITH_LEVEL_PATTERN.matcher(cardSpecifier.trim());
        if (!matcher.matches()) {
            return null;
        }

        String cardName = matcher.group(1).trim();
        if (cardName.contains(CardName.CARD_NAME_NUMBER_SEPARATOR)) {
            return null;
        }

        return new NameWithLevel(cardName, Integer.parseInt(matcher.group(2)));
    }

    public boolean matchesNameWithLevel(NameWithLevel ref) {
        return name.toString().equalsIgnoreCase(ref.name()) && getLevel() == ref.level();
    }

    public String toNameWithLevelSpecifier() {
        return name.toString() + " lvl" + getLevel();
    }

    public static MonsterCard findByNameWithLevel(CardGroup<MonsterCard> cards, NameWithLevel ref) {
        for (MonsterCard card : cards.iterable()) {
            if (card.matchesNameWithLevel(ref)) {
                return card;
            }
        }
        return null;
    }

    public static MonsterCard findByNameWithLevel(CardGroup<MonsterCard> cards,
            String cardSpecifier) {
        NameWithLevel ref = parseNameWithLevel(cardSpecifier);
        if (ref == null) {
            return null;
        }
        return findByNameWithLevel(cards, ref);
    }

    /** Returns copies of this card's move slots, optionally including empty slots. */
    public List<Move> getAllMoves(boolean includeEmpty) {
        List<Move> movesList = new ArrayList<>();
        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            if (includeEmpty || !moves[moveIndex].isEmpty()) {
                movesList.add(moves[moveIndex].copy());
            }
        }
        return movesList;
    }

    public int getNumMoves() {
        return numMoves;
    }

    /**
     * Sets how many move slots are active. Trailing slots beyond the new count are cleared.
     * Expanding the count exposes existing (cleared) slots as empty until setMove fills them.
     */
    public boolean setNumMoves(int numMoves) {
        return setNumMoves(numMoves, false, null);
    }

    public boolean setNumMoves(int numMoves, WarningCollector warnings) {
        return setNumMoves(numMoves, false, warnings);
    }

    /**
     * Sets how many move slots are active. Reducing the count refuses to clear locked assignment
     * slots unless forceOverride is true.
     */
    public boolean setNumMoves(int numMoves, boolean forceOverride, WarningCollector warnings) {
        if (numMoves < 0 || numMoves > MAX_NUM_MOVES) {
            return false;
        }

        if (numMoves < this.numMoves) {
            for (int moveIndex = numMoves; moveIndex < this.numMoves; moveIndex++) {
                if (moves[moveIndex].isLockedViaAssignment()) {
                    if (!forceOverride) {
                        warnLockedMoveClearBlocked(moveIndex, warnings);
                        return false;
                    }
                    Logger.info("Clearing locked assignment on \"" + toNameWithLevelSpecifier()
                            + "\" in slot " + (moveIndex + 1) + " by reducing move count.");
                }
                moves[moveIndex].makeEmpty();
            }
        }
        this.numMoves = numMoves;
        return true;
    }

    private void warnLockedMoveClearBlocked(int moveIndex, WarningCollector warnings) {
        String message = "Refusing to reduce move count on \"" + toNameWithLevelSpecifier()
                + "\" because slot " + (moveIndex + 1) + " has a locked assignment.";
        if (warnings != null) {
            warnings.addWarning(message);
        } else {
            Logger.warn(message);
        }
    }

    /** Returns the 0-based indexes of active move slots locked via assignment. */
    public List<Integer> getLockedMoveIndexes() {
        List<Integer> lockedIndexes = new ArrayList<>();
        for (int moveIndex = 0; moveIndex < numMoves; moveIndex++) {
            if (moves[moveIndex].isLockedViaAssignment()) {
                lockedIndexes.add(moveIndex);
            }
        }
        return lockedIndexes;
    }

    /** Returns the highest 0-based index of an active move slot locked via assignment. */
    public int getMaxLockedMoveIndex() {
        int maxLockedIndex = -1;
        for (int moveIndex = 0; moveIndex < numMoves; moveIndex++) {
            if (moves[moveIndex].isLockedViaAssignment()) {
                maxLockedIndex = Math.max(maxLockedIndex, moveIndex);
            }
        }
        return maxLockedIndex;
    }

    /** Returns a copy of the move in the given slot, which may be empty. */
    public Move getMove(int moveIndex) {
        if (moveIndex < 0 || moveIndex >= MAX_NUM_MOVES) {
            return null;
        }

        return moves[moveIndex].copy();
    }

    public Move getMoveWithName(String moveName) {
        for (int moveIndex = 0; moveIndex < numMoves; moveIndex++) {
            Move move = moves[moveIndex];
            if (move.name.toString().equals(moveName)) {
                return move.copy();
            }
        }
        return null;
    }

    /**
     * Copies move data into the slot and updates numMoves so it remains the source of truth for
     * active slots. Locked assignment slots are left unchanged unless {@code forceOverride} is
     * true.
     */
    public boolean setMove(Move move, int moveSlot) {
        return setMove(move, moveSlot, false, null);
    }

    public boolean setMove(Move move, int moveSlot, boolean forceOverride) {
        return setMove(move, moveSlot, forceOverride, null);
    }

    public boolean setMove(Move move, int moveSlot, boolean forceOverride,
            WarningCollector warnings) {
        if (moveSlot < 0 || moveSlot >= moves.length) {
            return false;
        }

        if (moves[moveSlot].isLockedViaAssignment()) {
            if (!forceOverride) {
                warnLockedMoveOverwriteBlocked(moveSlot, warnings);
                return false;
            }
            Logger.info("Overwriting locked assignment on \"" + toNameWithLevelSpecifier()
                    + "\" in slot " + (moveSlot + 1) + ".");
        }
        moves[moveSlot].copyNonMetadataFieldsFrom(move);
        // Leave the locked status unchanged
        updateNumMovesForSlot(moveSlot);
        return true;
    }

    private void warnLockedMoveOverwriteBlocked(int moveIndex, WarningCollector warnings) {
        String message = "Refusing to overwrite locked assignment on \""
                + toNameWithLevelSpecifier() + "\" in slot " + (moveIndex + 1) + ".";
        if (warnings != null) {
            warnings.addWarning(message);
        } else {
            Logger.warn(message);
        }
    }

    public void setMoveLockedViaAssignment(int moveSlot, boolean locked) {
        if (moveSlot >= 0 && moveSlot < moves.length) {
            moves[moveSlot].setLockedViaAssignment(locked);
        }
    }

    private void updateNumMovesForSlot(int moveSlot) {
        if (!moves[moveSlot].isEmpty()) {
            numMoves = Math.max(numMoves, moveSlot + 1);
        } else {
            trimTrailingEmptyMoves();
        }
    }

    public List<Integer> setMoves(Move... newMoves) {
        return setMoves(List.of(newMoves));
    }

    /**
     * Replaces move slots up to {@link #MAX_NUM_MOVES} and sets numMoves to the highest non-empty
     * slot index + 1. Shorter lists clear the remaining slots.
     *
     * @return 0-based indexes of slots that were successfully updated
     */
    public List<Integer> setMoves(List<Move> newMoves) {
        return setMoves(newMoves, false, null);
    }

    public List<Integer> setMoves(List<Move> newMoves, boolean forceOverride) {
        return setMoves(newMoves, forceOverride, null);
    }

    public List<Integer> setMoves(List<Move> newMoves, boolean forceOverride,
            WarningCollector warnings) {
        if (newMoves.size() > moves.length) {
            throw new IllegalArgumentException(
                    "Bad number of moves (" + newMoves.size() + ") was passed!");
        }

        List<Integer> setIndexes = new ArrayList<>();
        for (int moveIndex = 0; moveIndex < newMoves.size(); moveIndex++) {
            if (setMove(newMoves.get(moveIndex), moveIndex, forceOverride, warnings)) {
                setIndexes.add(moveIndex);
            }
        }
        for (int moveIndex = newMoves.size(); moveIndex < moves.length; moveIndex++) {
            if (setMove(new Move(this, moveIndex), moveIndex, forceOverride, warnings)) {
                setIndexes.add(moveIndex);
            }
        }
        return setIndexes;
    }

    private void trimTrailingEmptyMoves() {
        while (numMoves > 0 && moves[numMoves - 1].isEmpty()) {
            numMoves--;
        }
    }

    public void sortMoves() {
        boolean needsSwap;
        for (int moveIndex = 0; moveIndex < moves.length - 1; moveIndex++) {
            needsSwap = false;
            // Move empty moves to the end
            if (moves[moveIndex].isEmpty() || moves[moveIndex + 1].isEmpty()) {
                if (moves[moveIndex].isEmpty() && !moves[moveIndex + 1].isEmpty()) {
                    needsSwap = true;
                }
            }
            // Move poke powers first
            else if (!moves[moveIndex].isPokePower() && moves[moveIndex + 1].isPokePower()) {
                needsSwap = true;
            } else {
                int numColorless1 = moves[moveIndex].getCost(EnergyType.COLORLESS);
                int numColorless2 = moves[moveIndex + 1].getCost(EnergyType.COLORLESS);
                int numNonColorless1 = moves[moveIndex].getNonColorlessEnergyCosts();
                int numNonColorless2 = moves[moveIndex + 1].getNonColorlessEnergyCosts();

                // Move higher total energies last
                if (numColorless1 + numNonColorless1 > numColorless2 + numNonColorless2) {
                    needsSwap = true;
                } else if (numColorless1 + numNonColorless1 == numColorless2 + numNonColorless2) {
                    // If equal num, move more non-colorless last
                    if (numNonColorless1 > numNonColorless2) {
                        needsSwap = true;
                    } else if (numNonColorless1 == numNonColorless2) {
                        // If equal move higher damage last
                        if (moves[moveIndex].damage > moves[moveIndex + 1].damage) {
                            needsSwap = true;
                        }
                        // If equal, moves with effects last
                        else if (moves[moveIndex].damage == moves[moveIndex + 1].damage
                                && !moves[moveIndex].description.isEmpty()
                                && moves[moveIndex + 1].description.isEmpty()) {
                            needsSwap = true;
                        }
                    }
                }
            }

            if (needsSwap) {
                moves[moveIndex].swapNonMetadataFieldsWith(moves[moveIndex + 1]);
                moveIndex = 0; // restart sort loop
            }
        }
        // After packing empties to the end, keep numMoves aligned with occupied slots
        numMoves = 0;
        for (int moveIndex = 0; moveIndex < moves.length; moveIndex++) {
            if (!moves[moveIndex].isEmpty()) {
                numMoves = moveIndex + 1;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString() + "\nPokedex Number = " + dexNumber + "\nDesciption = "
                + description.toString() + "\nHP = " + getHp() + "\nStage = " + stage
                + "\nPrevEvolution = " + prevEvoName.toString() + "\nRetreatCost = " + retreatCost
                + "\nWeakness = " + weakness + "\nResistance = " + resistance + "\nMoves");

        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            builder.append("\n" + moves[moveIndex].toString());
        }
        return builder.toString();
    }

    @Override
    public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) {
        commonReadAndConvertIds(cardBytes, startIndex, idToText);

        int index = startIndex + Card.CARD_COMMON_SIZE;
        setHp(cardBytes[index++]);
        stage = EvolutionStage.readFromByte(cardBytes[index++]);

        // Read the prev evolution
        index = prevEvoName.readDataAndConvertIds(cardBytes, index, idToText);

        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            index = moves[moveIndex].readDataAndConvertIds(cardBytes, index, name, idToText);
        }
        numMoves = 0;
        for (Move move : moves) {
            if (!move.isEmpty()) {
                numMoves++;
            }
        }

        retreatCost = cardBytes[index++];
        weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
        resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);

        index = monsterCategory.readDataAndConvertIds(cardBytes, index, idToText);

        dexNumber = cardBytes[index++];
        unknownByte1 = cardBytes[index++];
        level = cardBytes[index++];
        lengthFt = cardBytes[index++];
        lengthIn = cardBytes[index++];
        weight = ByteUtils.readAsShort(cardBytes, index);
        index += 2;

        index = description.readDataAndConvertIds(cardBytes, index, idToText);

        unknownByte2 = cardBytes[index++];

        return TOTAL_SIZE_IN_BYTES;
    }

    @Override
    public void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks,
            InstructionParser parser) {
        commonFinalizeAndAddData(texts);

        prevEvoName.finalizeAndAddTexts(texts);
        monsterCategory.finalizeAndAddTexts(texts);
        description.finalizeAndAddTexts(texts);

        sortMoves();
        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            moves[moveIndex].finalizeAndAddData(cards, texts, blocks, this, parser);
        }
    }

    @Override
    protected CodeBlock convertToCodeBlock() {
        CodeBlock block = convertCommonDataToCodeBlock();

        RawBytePacker bytes = new RawBytePacker();
        bytes.append(getHp(), stage.getValue());
        bytes.append(ByteUtils.shortToLittleEndianBytes(prevEvoName.getTextId()));
        block.appendInstruction(bytes.createRawByteInsruct());

        for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++) {
            moves[moveIndex].appendToCodeBlock(block);
        }

        bytes = new RawBytePacker();
        bytes.append(retreatCost, weakness.getValue(), resistance.getValue());
        bytes.append(ByteUtils.shortToLittleEndianBytes(monsterCategory.getTextId()));
        bytes.append(dexNumber, unknownByte1, level);
        bytes.append(lengthFt, lengthIn);
        bytes.append(ByteUtils.shortToLittleEndianBytes(weight));
        bytes.append(ByteUtils.shortToLittleEndianBytes(description.getTextId()));
        bytes.append(unknownByte2);
        block.appendInstruction(bytes.createRawByteInsruct());

        return block;
    }

    @Override
    public int getSize() {
        return TOTAL_SIZE_IN_BYTES;
    }

    public byte getHp() {
        return hp;
    }

    public boolean setHp(int hp) {
        // TODO: Enforce multiple of 10 and not too large (<= 120?)
        this.hp = (byte) hp;
        return true;
    }
}
