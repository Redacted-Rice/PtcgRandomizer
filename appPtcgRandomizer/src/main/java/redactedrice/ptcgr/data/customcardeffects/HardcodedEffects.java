package redactedrice.ptcgr.data.customcardeffects;

import java.util.HashMap;
import java.util.Map;

public class HardcodedEffects {
    private static HardcodedEffects singleton = new HardcodedEffects();

    // Effect name, card name, effect
    private Map<String, Map<String, CustomCardEffect>> cardNameUniqueEffects;
    // Effect name, energy type, effect
    // private Map<String, Map<EnergyType, CustomCardEffect>> energyTypeUniqueEffects;
    // Effect name, effect
    // private Map<String, CustomCardEffect> globallyUniqueEffects;

    private HardcodedEffects() {
        cardNameUniqueEffects = new HashMap<>();
        // energyTypeUniqueEffects = new HashMap<>();
        // globallyUniqueEffects = new HashMap<>();
    }

    public static void reset() {
        singleton = new HardcodedEffects();
    }

    public static HardcodedEffects getInstance() {
        return singleton;
    }

    public CustomCardEffect tryGetCardNameUniqueEffect(String effectName, String cardName) {
        Map<String, CustomCardEffect> effectMap = cardNameUniqueEffects.get(effectName);
        if (effectMap == null) {
            return null;
        }

        return effectMap.get(cardName);
    }

    public void addCardNameUniqueEffect(String effectName, String cardName,
            CustomCardEffect effect) {
        Map<String, CustomCardEffect> effectMap = cardNameUniqueEffects.computeIfAbsent(effectName,
                k -> new HashMap<>());
        effectMap.put(cardName, effect);
    }

    // TODO later: Make so this can be read in from a file. Have a Constants file for all these
    // static things
    // then have more files that define the datablocks - i.e. their preferences/required locations
    // and
    // the code itself

    // TODO later: Have a move effect class that contains a uniqueness aspect of globally, type,
    // name, etc.
    // In that class have an effect command and effect pointers
    //
    // Have a higher level move effect tracker class that will pull out all the rom's move effects
    // based on
    // card and move name. Add in/replace the custom ones with that. Then if we allow file based
    // tweaking/specifying
    // of moves, we can refer to the ones already in the base rom
    // Then when we save, we can check the move name against our custom set of moves and write the
    // data if needed
    // or get a reference to the existing data

    // Things might get trickier if move names are reused with different effects (i.e. discard 1 vs
    // discard 2)

    // public static String CARD_NAME_PLACEHOLDER = CompilerUtils.createPlaceholder("cardname");
    // public static String CARD_ID_PLACEHOLDER = CompilerUtils.createPlaceholder("cardid");

    // public static String FUNC_CREATE_DECK_CARD_LIST =
    // CompilerUtils.createPlaceholder("CreateDeckCardList");
    public static final String FUNC_CREATE_DECK_CARD_LIST_ADDR = "$11df";

    // public static String FUNC_GET_CARD_ID_FROM_DECK_INDEX =
    // CompilerUtils.createPlaceholder("GetCardIDFromDeckIndex");
    public static final String FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR = "$1324";

    // public static String FUNC_GET_TURN_DUELIST_VAR =
    // CompilerUtils.createPlaceholder("GetTurnDuelistVariable");
    public static final String FUNC_GET_TURN_DUELIST_VAR_ADDR = "$160b";

    // public static String FUNC_LOOK_FOR_CARDS_IN_DECK =
    // CompilerUtils.createPlaceholder("LookForCardsInDeck");
    public static final String FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR = "$2c2ec";

    // public static String FUNC_DISPLAY_CARD_LIST =
    // CompilerUtils.createPlaceholder("DisplayCardList");
    public static final String FUNC_DISPLAY_CARD_LIST_BANK1ADDR = "$55f0";

    // public static String FUNC_SET_CARD_LIST_HEADER_TEXT =
    // CompilerUtils.createPlaceholder("SetCardListHeaderText");
    public static final String FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR = "$5580";

    // public static String VAR_DUEL_TEMP_LIST = CompilerUtils.createPlaceholder("wDuelTempList");
    public static final String VAR_DUEL_TEMP_LIST_ADDR = "$c510";

    // public static String CONST_DECK_SIZE = CompilerUtils.createPlaceholder("DECK_SIZE");
    public static final String CONST_DECK_SIZE_VAL = "$3C"; // 60 in hex

    // public static String CONST_CARD_LOCATION_DECK =
    // CompilerUtils.createPlaceholder("CARD_LOCATION_DECK");
    public static final String CONST_CARD_LOCATION_DECK_VAL = "$0";

    // public static String CONST_DUELVARS_CARD_LOCATIONS =
    // CompilerUtils.createPlaceholder("DUELVARS_CARD_LOCATIONS");
    public static final String CONST_DUELVARS_CARD_LOCATIONS_VAL = "$0";

    // public static String CONST_SEARCHEFFECT_CARD_ID =
    // CompilerUtils.createPlaceholder("SEARCHEFFECT_CARD_ID");
    public static final String CONST_SEARCHEFFECT_CARD_ID_VAL = "$0";

    // public static final String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND =
    // CompilerUtils.createPlaceholder("SearchCardInDeckAndAddToHand");
    public static final String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND_ADDR = "$10fc";

    // public static final String FUNC_ADD_CARD_TO_HAND =
    // CompilerUtils.createPlaceholder("AddCardToHand");
    public static final String FUNC_ADD_CARD_TO_HAND_ADDR = "$1123";

    // public static final String FUNC_PUT_HAND_MONSTER_IN_PLAY_ADDR =
    // CompilerUtils.createPlaceholder("PutHandMonsterCardInPlayArea");
    public static final String FUNC_PUT_HAND_MONSTER_IN_PLAY_ADDR = "$1485";

    // public static final String FUNC_IS_PLAYER_TURN =
    // CompilerUtils.createPlaceholder("IsPlayerTurn");
    public static final String FUNC_IS_PLAYER_TURN_ADDR = "$2c0c7";

    // public static final String FUNC_DISPLAY_CARD_DETAILS =
    // CompilerUtils.createPlaceholder("DisplayCardDetailScreen");
    public static final String FUNC_DISPLAY_CARD_DETAILS_BANK1ADDR = "$4b31";

    static void replaceAllInByteArray(byte[] array, byte toFind, byte replaceWith) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == toFind) {
                array[i] = replaceWith;
            }
        }
    }
}
