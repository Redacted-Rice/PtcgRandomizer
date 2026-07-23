package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import java.util.List;

// Resolves the display values for an enum referenced by name (the ENUM base type, e.g.
// definition = { type = "enum", constraint = "SomeEnum" }, registered via context.registerEnum
// in a module's onLoad). Kept as a narrow interface instead of exposing the whole
// LuaRandomizerWrapper/EnumRegistry so this package doesn't need to depend on Lua execution
// internals - callers can just supply a method reference like actionBank::getEnumValues.
public interface EnumValuesProvider {
    // Returns the registered values for the named enum, or null/empty if it isn't registered.
    List<String> getEnumValues(String enumName);
}
