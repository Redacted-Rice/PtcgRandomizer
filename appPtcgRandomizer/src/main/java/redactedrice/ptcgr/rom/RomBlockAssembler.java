package redactedrice.ptcgr.rom;


import java.util.List;

import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.InstructionParser;
import redactedrice.compiler.instructions.addressref.BlockBankLoadedAddress;
import redactedrice.compiler.instructions.addressref.BlockGlobalAddress;
import redactedrice.compiler.instructions.basic.RawBytes;
import redactedrice.gbcframework.addressing.AddressRange;
import redactedrice.ptcgr.compiler.PtcgInstructionSetParser;
import redactedrice.ptcgr.constants.CharMapConstants;
import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.CardEffect;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.data.Move;
import redactedrice.ptcgr.data.customcardeffects.CustomCardEffect;
import redactedrice.rompacker.Blocks;
import redactedrice.rompacker.FixedBlock;
import redactedrice.rompacker.HybridBlock;
import redactedrice.rompacker.MovableBlock;

public final class RomBlockAssembler {
    private RomBlockAssembler() {}

    public static void assemble(Cards cards, Texts texts, Blocks blocks, RomSourceMap sourceMap,
            InstructionParser parser, PtcgInstructionSetParser ptcgParser) {
        finalizeAndAddAllTexts(cards, texts);
        addEffectBlocks(cards, blocks, parser);
        addCardBlocks(cards, blocks, sourceMap);
        ptcgParser.finalizeAndAddTexts(texts);
        addTextBlocks(texts, blocks, sourceMap);
    }

    private static void finalizeAndAddAllTexts(Cards cards, Texts texts) {
        for (Card card : cards.cards().listOrderedByCardId()) {
            card.finalizeAndAddTexts(texts);
        }
    }

    private static void addEffectBlocks(Cards cards, Blocks blocks, InstructionParser parser) {
        boolean needsMultibankTweak = false;
        for (MonsterCard card : cards.cards().monsterCards().listOrderedByCardId()) {
            for (int moveIndex = 0; moveIndex < MonsterCard.MAX_NUM_MOVES; moveIndex++) {
                Move move = card.moveAt(moveIndex);
                CardEffect cardEffect = move.getEffect();
                if (!(cardEffect instanceof CustomCardEffect customEffect)) {
                    continue;
                }

                List<MovableBlock> effectBlocks = customEffect.convertToBlocks();
                for (MovableBlock block : effectBlocks) {
                    blocks.addMovableBlock(block);
                }
                if (customEffect.hasCompiledFunctions()) {
                    needsMultibankTweak = true;
                }
            }
        }
        if (needsMultibankTweak) {
            CustomCardEffect.addTweakToAllowEffectsInMoreBanks(blocks, parser);
        }
    }

    private static void addCardBlocks(Cards cards, Blocks blocks, RomSourceMap sourceMap) {
        CodeBlock cardPtrs =
                newPointerTable("internal_cardPointers", sourceMap.getCardPointerTable());
        cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));

        for (Card card : cards.cards().iterable()) {
            HybridBlock cardBlock = toCardHybridBlock(card, sourceMap);
            blocks.addHybridBlock(cardBlock);
            cardPtrs.appendInstruction(
                    new BlockBankLoadedAddress(cardBlock.getMovableBlock().getId(), false));
        }

        cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));
        blocks.addFixedBlock(new FixedBlock(cardPtrs, PtcgRomConstants.CARD_POINTERS_LOC));
    }

    private static void addTextBlocks(Texts texts, Blocks blocks, RomSourceMap sourceMap) {
        CodeBlock textPtrs =
                newPointerTable("internal_textPointers", sourceMap.getTextPointerTable());
        textPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0, (byte) 0));

        String nullTextLabel = "";
        int usedCount = 1;
        short textId = 1;
        for (; usedCount < texts.count(); textId++) {
            if (!texts.hasTextId(textId)) {
                if (nullTextLabel.isEmpty()) {
                    nullTextLabel = "internal_romTextNull";
                    addTextBlock(texts, textId, nullTextLabel, blocks, sourceMap);
                }

                textPtrs.appendInstruction(new BlockGlobalAddress(nullTextLabel,
                        PtcgRomConstants.TEXT_POINTER_OFFSET));
                continue;
            }

            String textLabel = "internal_romText_" + textId;
            textPtrs.appendInstruction(
                    new BlockGlobalAddress(textLabel, PtcgRomConstants.TEXT_POINTER_OFFSET));
            addTextBlock(texts, textId, textLabel, blocks, sourceMap);
            usedCount++;
        }

        blocks.addFixedBlock(new FixedBlock(textPtrs, PtcgRomConstants.TEXT_POINTERS_LOC));
    }

    private static CodeBlock newPointerTable(String id, AddressRange pointerTableRange) {
        CodeBlock ptrs = new CodeBlock(id);
        if (pointerTableRange != null) {
            ptrs.addByteSourceHint(pointerTableRange);
        }
        return ptrs;
    }

    private static HybridBlock toCardHybridBlock(Card card, RomSourceMap sourceMap) {
        CodeBlock code = card.convertToCodeBlock();
        AddressRange origRange = sourceMap.getCardRange(card.id);
        if (origRange != null) {
            code.addByteSourceHint(origRange);
            return new HybridBlock(new MovableBlock(code, 0, (byte) 0xC, (byte) 0xD),
                    origRange.getStart());
        }
        return new HybridBlock(new MovableBlock(code, 0, (byte) 0xC, (byte) 0xD), -1);
    }

    private static void addTextBlock(Texts texts, short textId, String textLabel, Blocks blocks,
            RomSourceMap sourceMap) {
        byte[] stringBytes = texts.getAtId(textId).getBytes();

        CodeBlock text = new CodeBlock(textLabel);
        if (stringBytes.length > 0) {
            text.appendInstruction(new RawBytes(stringBytes));
        }
        text.appendInstruction(new RawBytes((byte) CharMapConstants.TEXT_END_CHAR));
        MovableBlock block = new MovableBlock(text, 1, (byte) 0xd, (byte) 0x1c);

        AddressRange origRange = sourceMap.getTextRange(textId);
        if (origRange != null) {
            blocks.addHybridBlock(new HybridBlock(block, origRange.getStart()));
            text.addByteSourceHint(origRange);
        } else {
            blocks.addMovableBlock(block);
        }
    }
}
