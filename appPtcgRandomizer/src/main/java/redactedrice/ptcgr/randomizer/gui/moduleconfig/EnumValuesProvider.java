package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;

import redactedrice.randomizer.context.EnumDefinition;

// Resolves enum values and value display labels for the ENUM base type. Prefer passing ActionBank
// itself so value display name lookups use the real implementations.
public interface EnumValuesProvider {
    // Returns the registered values for the named enum, or null/empty if it isn't registered.
    List<String> getEnumValues(String enumName);

    // Returns the registered label for one enum value, or the canonical value if none was registered.
    String getEnumValueDisplayName(String enumName, String canonicalValue);

    // Full definition when available so allow/exclude filters can match display labels.
    default EnumDefinition getEnumDefinition(String enumName) {
        return null;
    }
}
