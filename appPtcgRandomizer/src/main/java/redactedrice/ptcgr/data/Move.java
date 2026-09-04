package redactedrice.ptcgr.data;


import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.RawBytePacker;
import redactedrice.compiler.instructions.basic.RawBytes;
import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.romenums.*;
import redactedrice.ptcgr.data.romtexts.EffectDescription;
import redactedrice.ptcgr.data.romtexts.MoveName;
import redactedrice.ptcgr.data.romtexts.RomText;
import redactedrice.ptcgr.data.support.MoveBasicSorter;
import redactedrice.ptcgr.rom.Texts;

public class Move {
    public static final int TOTAL_SIZE_IN_BYTES = 19;
    public static final Comparator<Move> BASIC_SORTER = new MoveBasicSorter();

    public EnumMap<EnergyType, Byte> energyCost;
    public MoveName name;
    public EffectDescription description;
    public byte damage; // TODO: non multiple of 10?
    public MoveCategory category;
    public CardEffect effect;
    public Set<MoveEffectFlags1> effectFlags1;
    public Set<MoveEffectFlags2> effectFlags2;
    public Set<MoveEffectFlags3> effectFlags3;
    public byte effectParam;
    public byte animation;
    private final MonsterCard sourceCard;
    private final int sourceMoveIndex;
    private boolean lockedViaAssignment;

    public Move(MonsterCard card, int moveIndex) {
        sourceCard = Objects.requireNonNull(card);
        if (moveIndex < 0 || moveIndex >= MonsterCard.MAX_NUM_MOVES) {
            throw new IllegalArgumentException("Move slot index out of range: " + moveIndex);
        }
        sourceMoveIndex = moveIndex;
        initializeEmptyFields();
    }

    private void initializeEmptyFields() {
        energyCost = new EnumMap<>(EnergyType.class);
        name = new MoveName();
        description = new EffectDescription();
        category = MoveCategory.DAMAGE_NORMAL;
        effect = ExistingCardEffect.NONE;
        effectFlags1 = new HashSet<>();
        effectFlags2 = new HashSet<>();
        effectFlags3 = new HashSet<>();
        effectParam = 0;
    }

    public void copyNonMetadataFieldsFrom(Move toCopy) {
        energyCost = new EnumMap<>(toCopy.energyCost);
        name = new MoveName(toCopy.name);
        description = new EffectDescription(toCopy.description);
        damage = toCopy.damage;
        category = toCopy.category;
        effect = toCopy.effect.copy();
        effectFlags1 = new HashSet<>(toCopy.effectFlags1);
        effectFlags2 = new HashSet<>(toCopy.effectFlags2);
        effectFlags3 = new HashSet<>(toCopy.effectFlags3);
        effectParam = toCopy.effectParam;
        animation = toCopy.animation;
    }

    public void swapNonMetadataFieldsWith(Move other) {
        Move temp = new Move(sourceCard, sourceMoveIndex);
        temp.copyNonMetadataFieldsFrom(this);
        copyNonMetadataFieldsFrom(other);
        other.copyNonMetadataFieldsFrom(temp);
    }

    public void makeEmpty() {
        initializeEmptyFields();
        lockedViaAssignment = false;
    }

    public Move copy() {
        Move move = new Move(sourceCard, sourceMoveIndex);
        move.lockedViaAssignment = lockedViaAssignment;
        move.copyNonMetadataFieldsFrom(this);
        return move;
    }

    public boolean isLockedViaAssignment() {
        return lockedViaAssignment;
    }

    void setLockedViaAssignment(boolean lockedViaAssignment) {
        this.lockedViaAssignment = lockedViaAssignment;
    }

    public MonsterCard getSourceCard() {
        return sourceCard;
    }

    public int getSourceMoveIndex() {
        return sourceMoveIndex;
    }

    public String getEffectSortKey() {
        return effect.toString();
    }

    public boolean isEmpty() {
        return name.isEmpty();
    }

    public boolean isAttack() {
        return !isEmpty() && !isPokePower();
    }

    public boolean doesDamage() {
        if (isAttack()) {
            // If its listed as doing damage or is one of the moves that does damage just doesn't
            // have an associated damage number, this will return true
            return damage > 0
                    || PtcgRomConstants.ZERO_DAMAGE_DAMAGING_MOVES.contains(name.toString());
        }

        return false;
    }

    public boolean isPokePower() {
        return !isEmpty() && MoveCategory.POWER == category;
    }

    public boolean hasEffect() {
        return !description.isEmpty();
    }

    public String getEnergyCostString(boolean abbreviated, String separator) {
        StringBuilder sb = new StringBuilder();
        energyCostsAsString(sb, abbreviated, separator);
        return sb.toString();
    }

    public void energyCostsAsString(StringBuilder string, boolean abbreviated, String separator) {
        boolean foundEnergy = false;
        for (EnergyType energyType : EnergyType.values()) {
            if (energyCost.get(energyType) != null && energyCost.get(energyType) > 0) {
                if (foundEnergy) {
                    string.append(separator);
                }
                string.append(energyCost.get(energyType));
                string.append(" ");
                if (abbreviated) {
                    string.append(energyType.getAbbreviation());
                } else {
                    string.append(energyType);
                }
                foundEnergy = true;
            }
        }
        if (!foundEnergy) {
            string.append("None");
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Move Name: ");
        builder.append(name.toString());
        builder.append("\nMove Category: ");
        builder.append(category);
        builder.append("\nEnergies Required:\n\t");

        // Full energy names, separate with newline and tab
        energyCostsAsString(builder, false, "\n\t");

        builder.append("\nDamage:");
        builder.append(damage);
        builder.append("\nDescription: ");
        builder.append(description.toString());
        builder.append("\nEffectPtr: ");
        builder.append(effect.toString());
        builder.append("\nEffectFlags: ");
        builder.append(effectFlags1);
        builder.append(", ");
        builder.append(effectFlags2);
        builder.append(", ");
        builder.append(effectFlags3);

        return builder.toString();
    }

    public String getDamageString() {
        if (hasEffect()) {
            if (damage == 0) {
                return "-*";
            }
            return damage + "*";
        }
        return damage + " ";
    }

    public byte getCost(EnergyType inType) {
        if (energyCost.get(inType) != null) {
            return energyCost.get(inType);
        } else {
            return 0;
        }
    }

    public byte getNonColorlessEnergyCosts() {
        byte energyCount = 0;
        for (EnergyType energyType : EnergyType.values()) {
            if (energyType != EnergyType.COLORLESS) {
                energyCount += getCost(energyType);
            }
        }

        return energyCount;
    }

    public void clearCosts() {
        energyCost.clear();
    }

    public void setCost(EnergyType inType, byte inCost) {
        if (inCost > ByteUtils.MAX_HEX_CHAR_VALUE || inCost < ByteUtils.MIN_BYTE_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid value was passed for energy type " + inType + " cost: " + inCost);
        }
        energyCost.put(inType, inCost);
    }

    public int readDataAndConvertIds(byte[] moveBytes, int startIndex, RomText cardName,
            Texts idToText) {
        int index = startIndex;

        // They are stored in octects corresponding to their energy type. Since we
        // read them as bytes, we mask each byte and increment the index every other time
        energyCost = new EnumMap<>(EnergyType.class);
        setCost(EnergyType.FIRE, ByteUtils.readUpperHexChar(moveBytes[index]));
        setCost(EnergyType.GRASS, ByteUtils.readLowerHexChar(moveBytes[index]));
        index++;
        setCost(EnergyType.LIGHTNING, ByteUtils.readUpperHexChar(moveBytes[index]));
        setCost(EnergyType.WATER, ByteUtils.readLowerHexChar(moveBytes[index]));
        index++;
        setCost(EnergyType.FIGHTING, ByteUtils.readUpperHexChar(moveBytes[index]));
        setCost(EnergyType.PSYCHIC, ByteUtils.readLowerHexChar(moveBytes[index]));
        index++;
        setCost(EnergyType.COLORLESS, ByteUtils.readUpperHexChar(moveBytes[index]));
        setCost(EnergyType.UNUSED_TYPE, ByteUtils.readLowerHexChar(moveBytes[index]));
        index++;

        index = name.readDataAndConvertIds(moveBytes, index, idToText);

        int[] descIndexes = {index, index + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES};
        description.readDataAndConvertIds(moveBytes, descIndexes, cardName, idToText);
        index += PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;

        damage = moveBytes[index++];
        category = MoveCategory.readFromByte(moveBytes[index++]);
        effect = new ExistingCardEffect(ByteUtils.readAsShort(moveBytes, index));
        index += 2;
        effectFlags1 = MoveEffectFlags1.readFromByte(moveBytes[index++]);
        effectFlags2 = MoveEffectFlags2.readFromByte(moveBytes[index++]);
        effectFlags3 = MoveEffectFlags3.readFromByte(moveBytes[index++]);
        effectParam = moveBytes[index++];
        animation = moveBytes[index++];

        return index;
    }

    public void finalizeAndAddTexts(Texts texts, MonsterCard hostCard) {
        name.finalizeAndAddTexts(texts);
        description.finalizeAndAddTexts(texts, hostCard.name.toString());
    }

    public CardEffect getEffect() {
        return effect;
    }

    public void appendToCodeBlock(CodeBlock block) {
        RawBytePacker bytes = new RawBytePacker();
        bytes.append(
                ByteUtils.packHexCharsToByte(getCost(EnergyType.FIRE), getCost(EnergyType.GRASS)),
                ByteUtils.packHexCharsToByte(getCost(EnergyType.LIGHTNING),
                        getCost(EnergyType.WATER)),
                ByteUtils.packHexCharsToByte(getCost(EnergyType.FIGHTING),
                        getCost(EnergyType.PSYCHIC)),
                ByteUtils.packHexCharsToByte(getCost(EnergyType.COLORLESS),
                        getCost(EnergyType.UNUSED_TYPE)));
        bytes.append(ByteUtils.shortToLittleEndianBytes(name.getTextId()));
        bytes.append(ByteUtils.shortListToLittleEndianBytes(
                description.getTextIds(PtcgRomConstants.MAX_BLOCKS_EFFECT_DESC)));
        bytes.append(damage, category.getValue());
        block.appendInstruction(bytes.createRawByteInsruct());

        effect.appendToCodeBlock(block);

        block.appendInstruction(new RawBytes(MoveEffectFlags1.storeAsByte(effectFlags1),
                MoveEffectFlags2.storeAsByte(effectFlags2),
                MoveEffectFlags3.storeAsByte(effectFlags3), effectParam, animation));
    }
}
