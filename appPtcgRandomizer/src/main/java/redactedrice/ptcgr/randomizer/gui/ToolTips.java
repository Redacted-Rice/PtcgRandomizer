package redactedrice.ptcgr.randomizer.gui;

// Formats Swing tooltip text so longer descriptions wrap instead of stretching off screen.
public final class ToolTips {
    private static final int DEFAULT_WRAP_WIDTH_PX = 320;

    private ToolTips() {}

    public static String wrapping(String text) {
        return wrapping(text, DEFAULT_WRAP_WIDTH_PX);
    }

    public static String wrapping(String text, int widthPx) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return "<html><body style='width:" + widthPx + "px'>" + escapeHtml(text.trim())
                + "</body></html>";
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
