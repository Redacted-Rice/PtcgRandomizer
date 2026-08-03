package redactedrice.ptcgr.configs;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import redactedrice.randomizer.utils.IssueTracker;

public final class ParserHelpers {
    private ParserHelpers() {}

    public static String entryContext(String sourceLabel, String entryPath) {
        return sourceLabel + ":" + entryPath;
    }

    public static void forEachEntryInList(Object rawList, String listFieldName, String sourceLabel, BiConsumer<Map<String, Object>, String> handler) {
        if (rawList == null) {
            return;
        }
        if (!(rawList instanceof List<?> entries)) {
            IssueTracker.addWarning(sourceLabel + ": \"" + listFieldName + "\" must be a list.");
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            String entryContext = entryContext(sourceLabel, listFieldName + "[" + i + "]");
            Object entry = entries.get(i);
            if (!(entry instanceof Map<?, ?> entryMap)) {
                IssueTracker.addWarning(entryContext + ": entry must be a mapping.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) entryMap;
            handler.accept(fields, entryContext);
        }
    }

    public static boolean parseBoolean(Object value, boolean defaultValue, String fieldName,
            String entryLabel) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }

        IssueTracker.addWarning(entryLabel + ": field \"" + fieldName
                + "\" must be a boolean; false will be assumed.");
        return false;
    }

    public static String parseOptionalString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    public static String parseRequiredString(Object value, String fieldName, String entryLabel) {
        if (value == null) {
            IssueTracker.addWarning(entryLabel + ": missing required field \"" + fieldName + "\".");
            return null;
        }

        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            IssueTracker.addWarning(entryLabel + ": required field \"" + fieldName + "\" is empty.");
            return null;
        }
        return trimmed;
    }

    public static Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
