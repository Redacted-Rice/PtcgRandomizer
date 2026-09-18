package redactedrice.ptcgr.data;

import java.util.EnumSet;
import java.util.Set;

import redactedrice.ptcgr.constants.romenums.CardId;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
import redactedrice.ptcgr.rom.Cards;

/**
 * Builds virtual monster cards for trainers that can be played as basic
 * monsters
 */
public final class TrainerMonsterProxyFactory {
    private static final Set<CardId> PROXY_TRAINER_IDS = EnumSet.of(CardId.TRAINER_B_70, CardId.TRAINER_F_62);

    private TrainerMonsterProxyFactory() {
    }

    public static void attachProxies(Cards cards) {
        for (NonMonsterCard trainer : cards.cards().trainerCards().listOrderedByCardId()) {
            if (!PROXY_TRAINER_IDS.contains(trainer.id)) {
                continue;
            }
            cards.addTrainerMonsterProxy(fromTrainer(trainer));
        }
    }

    public static MonsterCard fromTrainer(NonMonsterCard trainer) {
        MonsterCard proxy = new MonsterCard();
        proxy.isTrainerProxy = true;
        proxy.id = trainer.id;
        proxy.type = CardType.MONSTER_COLORLESS;
        proxy.stage = EvolutionStage.BASIC;
        proxy.name.setText(trainer.name.toString());
        proxy.gfx = trainer.gfx;
        proxy.rarity = trainer.rarity;
        proxy.set = trainer.set;
        proxy.pack = trainer.pack;
        return proxy;
    }
}
