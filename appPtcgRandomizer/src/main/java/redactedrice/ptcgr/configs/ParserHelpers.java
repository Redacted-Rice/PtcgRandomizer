package redactedrice.ptcgr.configs;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import redactedrice.ptcgr.utils.WarningCollector;

public final class ParserHelpers {
    private ParserHelpers() {}

    public static String entryContext(String sourceLabel, String entryPath) {
        return sourceLabel + ":" + entryPath;
    }

    public static void forEachEntryInList(Object rawList, String listFieldName, String sourceLabel,
            WarningCollector warnings, BiConsumer<Map<String, Object>, String> handler) {
        if (rawList == null) {
            return;
        }
        if (!(rawList instanceof List<?> entries)) {
            warnings.addWarning(sourceLabel + ": \"" + listFieldName + "\" must be a list.");
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            String entryContext = entryContext(sourceLabel, listFieldName + "[" + i + "]");
            Object entry = entries.get(i);
            if (!(entry instanceof Map<?, ?> entryMap)) {
                warnings.addWarning(entryContext + ": entry must be a mapping.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) entryMap;
            handler.accept(fields, entryContext);
        }
    }

    public static boolean parseBoolean(Object value, boolean defaultValue, String fieldName,
            String entryLabel, WarningCollector warnings) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }

        warnings.addWarning(entryLabel + ": field \"" + fieldName
                + "\" must be a boolean; false will be assumed.");
        return false;
    }

    public static String parseOptionalString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    public static String parseRequiredString(Object value, String fieldName, String entryLabel,
            WarningCollector warnings) {
        if (value == null) {
            warnings.addWarning(entryLabel + ": missing required field \"" + fieldName + "\".");
            return null;
        }

        String trimmed = value.toString().trim();
        if (trimmed.isEmpty()) {
            warnings.addWarning(entryLabel + ": required field \"" + fieldName + "\" is empty.");
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
