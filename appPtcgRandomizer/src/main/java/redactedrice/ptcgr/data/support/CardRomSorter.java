package redactedrice.ptcgr.data.support;

import java.util.Comparator;

import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.MonsterCard;

// Use when evolutions are randomized so related monsters can be kept adjacent in ROM order.
public final class CardRomSorter implements Comparator<Card> {
    @Override
    public int compare(Card c1, Card c2) {
        if (c1.type.isEnergyCard() || c2.type.isEnergyCard() || c1.type.isTrainerCard()
                || c2.type.isTrainerCard()) {
            return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
        }

        MonsterCard pc1 = (MonsterCard) c1;
        MonsterCard pc2 = (MonsterCard) c2;
        int pokedexCompare = ByteUtils.unsignedCompareBytes(pc1.dexNumber, pc2.dexNumber);
        if (pokedexCompare == 0) {
            return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
        }
        return pokedexCompare;
    }
}
