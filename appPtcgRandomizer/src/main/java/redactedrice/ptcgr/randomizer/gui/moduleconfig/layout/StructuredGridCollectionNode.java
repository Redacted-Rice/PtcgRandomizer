package redactedrice.ptcgr.randomizer.gui.moduleconfig.layout;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import redactedrice.randomizer.lua.arguments.TypeDefinition;

final class StructuredGridCollectionNode {
    final TypeDefinition type;
    final List<StructuredGridEntry> entries = new ArrayList<>();
    JButton addButton;

    StructuredGridCollectionNode(TypeDefinition type) {
        this.type = type;
    }
}
