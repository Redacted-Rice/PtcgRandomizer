package redactedrice.ptcgr.data.customcardeffects;

import java.util.Arrays;
import java.util.List;

import redactedrice.compiler.InstructionParser;
import redactedrice.gbcframework.utils.RomUtils;
import redactedrice.ptcgr.constants.CharMapConstants;
import redactedrice.ptcgr.constants.romenums.EffectFunctionTypes;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.CardGroup;

// TODO later: Handle cards with multiple IDs (e.g. 4 different prints of the same monster)
// TODO later: Handle multiple cards (e.g. nidoran male or female)
public final class CallForFamily {
    private CallForFamily() {}

    static final String EFFECT_NAME = "CallForFamily";

    // TODO later: Read from rom?
    static final int INITIAL_EFFECT_ADDRESS = 0x2cc40; // Bellsprout's but they are all the same
                                                       // even for nidoran
    static final int PUT_IN_PLAY_AREA_EFFECT_ADDRESS = 0x2ccc2; // Bellsprout's but they are all
                                                                // the same even for nidoran

    // Basics may not always be monster cards - take the fossil trainer for example
    public static CustomCardEffect createMoveEffect(/* Cards<Card> cards, */ CardGroup<Card> basics,
            InstructionParser parser) {
        Card toFindBasicOf = basics.listOrderedByCardId().get(0);

        CustomCardEffect effect = HardcodedEffects.getInstance()
                .tryGetCardNameUniqueEffect(EFFECT_NAME, toFindBasicOf.name.toString());
        if (effect != null) {
            return effect;
        }

        String cardName = toFindBasicOf.name.toString();
        String cardId = String.format("$%x", toFindBasicOf.id.getValue());

        List<String> playerSelectCode = Arrays.asList("CallForFamilyPS" + cardName + ":",
                "ld a, $ff", "ldh [$ffa0], a",
                "call " + HardcodedEffects.FUNC_CREATE_DECK_CARD_LIST_ADDR,
                "ldtx hl, halfTextBox:Choose a " + cardName + " from the Deck.",
                "ldtx bc, cardName:" + cardName,
                "lb de, " + HardcodedEffects.CONST_SEARCHEFFECT_CARD_ID_VAL + ", " + cardId,
                "call " + HardcodedEffects.FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR, "ret c",

                "bank1call $5591", "ldtx hl, halfTextBox:Choose a " + cardName + ".",
                "ldtx de, textbox:" + CharMapConstants.RAMNAME + "'s Deck",
                "bank1call " + HardcodedEffects.FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR,

                ".loop", "bank1call " + HardcodedEffects.FUNC_DISPLAY_CARD_LIST_BANK1ADDR,
                "jr c, .pressed_b",
                "call " + HardcodedEffects.FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR,
                "ld bc, " + cardId, "call $3090", "jr nz, .play_sfx",

                "ldh a, [$ff98]", "ldh [$ffa0], a", "or a", "ret",

                ".play_sfx", "call $3794", "jr .loop",

                ".pressed_b", "ld a, " + HardcodedEffects.CONST_DUELVARS_CARD_LOCATIONS_VAL,
                "call " + HardcodedEffects.FUNC_GET_TURN_DUELIST_VAR_ADDR, ".loop_b_press",
                "ld a, [hl]", "cp " + HardcodedEffects.CONST_CARD_LOCATION_DECK_VAL, "jr nz, .next",
                "ld a, l", "call " + HardcodedEffects.FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR,
                "ld bc, " + cardId, "call $3090", "jr z, .play_sfx", ".next", "inc l", "ld a, l",
                "cp " + HardcodedEffects.CONST_DECK_SIZE_VAL, "jr c, .loop_b_press",

                "ld a, $ff", "ldh [$ffa0], a", "or a", "ret");

        List<String> aiSelectCode = Arrays.asList("CallForFamilyAIS" + cardName + ":",
                "call " + HardcodedEffects.FUNC_CREATE_DECK_CARD_LIST_ADDR,
                "ld hl, " + HardcodedEffects.VAR_DUEL_TEMP_LIST_ADDR, ".loop_deck", "ld a, [hli]",
                "ldh [$ffa0], a", "cp $ff", "ret z",
                "call " + HardcodedEffects.FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR, "ld a, e",
                "cp " + cardId, "jr nz, .loop_deck", "ret");

        effect = new CustomCardEffect(EFFECT_NAME + toFindBasicOf.name.toString());
        effect.addEffectFunction(EffectFunctionTypes.INITIAL_EFFECT_1,
                RomUtils.convertToLoadedBankOffset(INITIAL_EFFECT_ADDRESS));
        effect.addEffectFunction(EffectFunctionTypes.AFTER_DAMAGE,
                RomUtils.convertToLoadedBankOffset(PUT_IN_PLAY_AREA_EFFECT_ADDRESS));
        effect.addEffectFunction(EffectFunctionTypes.REQUIRE_SELECTION, playerSelectCode, parser);
        effect.addEffectFunction(EffectFunctionTypes.AI_SELECTION, aiSelectCode, parser);

        HardcodedEffects.getInstance().addCardNameUniqueEffect(EFFECT_NAME,
                toFindBasicOf.name.toString(), effect);
        return effect;
    }
}
