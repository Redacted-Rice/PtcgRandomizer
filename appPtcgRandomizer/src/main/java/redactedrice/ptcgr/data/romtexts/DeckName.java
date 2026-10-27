package redactedrice.ptcgr.data.romtexts;

import redactedrice.ptcgr.constants.PtcgRomConstants;
import redactedrice.ptcgr.constants.romenums.CharSetPrefix;

public class DeckName extends OneLineText {
    public DeckName() {
        super(PtcgRomConstants.MAX_CHARS_DECK_NAME);
    }

    public DeckName(String text) {
        this();
        setText(text);
    }

    public DeckName(CharSetPrefix charSet, String text) {
        this();
        setText(charSet, text);
    }

    public DeckName(DeckName toCopy) {
        super(toCopy);
    }
}
