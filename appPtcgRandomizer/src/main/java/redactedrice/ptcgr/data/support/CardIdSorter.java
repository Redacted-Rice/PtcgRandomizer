package redactedrice.ptcgr.data.support;

import java.util.Comparator;

import redactedrice.gbcframework.utils.ByteUtils;
import redactedrice.ptcgr.data.Card;

public final class CardIdSorter implements Comparator<Card> {
    @Override
    public int compare(Card c1, Card c2) {
        return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
    }
}
