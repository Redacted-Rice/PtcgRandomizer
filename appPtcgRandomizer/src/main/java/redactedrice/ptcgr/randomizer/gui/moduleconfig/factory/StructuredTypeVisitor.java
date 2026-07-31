package redactedrice.ptcgr.randomizer.gui.moduleconfig.factory;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

interface StructuredTypeVisitor<T> {
    T visitList(TypeDefinition typeDef);

    T visitTable(TypeDefinition typeDef);

    T visitEnum(TypeDefinition typeDef);

    T visitBase(TypeDefinition typeDef);
}
