package redactedrice.ptcgr.randomizer.gui.moduleconfig.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentConstraintDescription;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.EnumValuesProvider;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredText;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.DiscreteChoiceEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.EnumEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.NumberFieldEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.NumericEditing;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.StringFieldEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.editor.UnsupportedValueEditor;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.StructuredText;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.layout.StructuredGridPanel;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.support.ModuleConfigGuiTestSupport;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.lua.Module;
import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

public class ArgumentEditorFactoryTest extends ModuleConfigGuiTestSupport {
    private static final List<String> ADJUSTMENT_WARNINGS = new ArrayList<>();

    @BeforeEach
    void captureAdjustmentWarnings() {
        ADJUSTMENT_WARNINGS.clear();
        NumericEditing.setNotifierForTests((parent, message) -> ADJUSTMENT_WARNINGS.add(message));
    }

    @Test
    void valueAdjustmentWarningsDescribeContext() {
        ArgumentDefinition rangeArg = new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2);
        ArgumentValueEditor rangeEditor = ArgumentEditorFactory.create(rangeArg);
        JTextField rangeField = (JTextField) rangeEditor.getComponent();

        rangeField.setText("5");
        simulateFocusLost(rangeField);
        assertEquals("2", rangeField.getText());
        assertEquals(1, ADJUSTMENT_WARNINGS.size());
        assertEquals("The entered value 5 was adjusted to 2. (allowed range 0\u20132)",
                ADJUSTMENT_WARNINGS.get(0));

        ADJUSTMENT_WARNINGS.clear();
        rangeField.setText("10");
        assertEquals(2, rangeEditor.getValue());
        assertEquals(1, ADJUSTMENT_WARNINGS.size());

        ArgumentDefinition stepArg = new ArgumentDefinition("step",
                TypeDefinition.integer(ArgumentConstraint.discreteRange(0, 100, 5)), 25);
        ArgumentValueEditor stepEditor = ArgumentEditorFactory.create(stepArg);
        JTextField stepField = (JTextField) stepEditor.getComponent();

        ADJUSTMENT_WARNINGS.clear();
        stepField.setText("7");
        assertEquals(5, stepEditor.getValue());
        assertEquals(1, ADJUSTMENT_WARNINGS.size());
        assertTrue(ADJUSTMENT_WARNINGS.get(0).contains("step 5"));

        ADJUSTMENT_WARNINGS.clear();
        stepEditor.setValue(25);
        assertEquals(0, ADJUSTMENT_WARNINGS.size());
    }

    @Test
    void anyIntegerConstraintUsesFullIntRangeAndCapsValues() {
        ArgumentDefinition argDef = new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof NumberFieldEditor);
        assertEquals("", ArgumentConstraintDescription.describe(argDef.getTypeDefinition()));

        ArgumentDefinition zeroToMax = new ArgumentDefinition("zeroToMax",
                TypeDefinition.integer(ArgumentConstraint.range(0, Integer.MAX_VALUE)), 0);
        assertEquals("0 - " + Integer.MAX_VALUE,
                ArgumentConstraintDescription.describe(zeroToMax.getTypeDefinition()));

        ArgumentDefinition minToHundred = new ArgumentDefinition("minToHundred",
                TypeDefinition.integer(ArgumentConstraint.range(Integer.MIN_VALUE, 100)), 0);
        assertEquals(Integer.MIN_VALUE + " - 100",
                ArgumentConstraintDescription.describe(minToHundred.getTypeDefinition()));

        JTextField field = (JTextField) editor.getComponent();
        field.setText("12345");
        assertEquals(12345, editor.getValue());

        field.setText(String.valueOf((long) Integer.MAX_VALUE + 1L));
        simulateFocusLost(field);
        assertEquals(String.valueOf(Integer.MAX_VALUE), field.getText());
        assertEquals(Integer.MAX_VALUE, editor.getValue());

        field.setText(String.valueOf((long) Integer.MIN_VALUE - 1L));
        simulateFocusLost(field);
        assertEquals(String.valueOf(Integer.MIN_VALUE), field.getText());
        assertEquals(Integer.MIN_VALUE, editor.getValue());
    }

    @Test
    void rangeConstraintCapsOutOfRangeValues() {
        ArgumentDefinition argDef = new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof NumberFieldEditor);
        assertEquals("0 - 2", ArgumentConstraintDescription.describe(argDef.getTypeDefinition()));

        JTextField field = (JTextField) editor.getComponent();
        field.setText("2");
        assertEquals(2, editor.getValue());

        field.setText("5");
        simulateFocusLost(field);
        assertEquals("2", field.getText());
        assertEquals(2, editor.getValue());

        field.setText("-3");
        simulateFocusLost(field);
        assertEquals("0", field.getText());
        assertEquals(0, editor.getValue());

        field.setText("10");
        assertEquals(2, editor.getValue());
    }

    @Test
    void discreteRangeEditorsChooseDropdownOrTextFieldByStepCount() {
        ArgumentDefinition dropdownArg = new ArgumentDefinition("step",
                TypeDefinition.integer(ArgumentConstraint.discreteRange(0, 10, 5)), 0);
        ArgumentValueEditor dropdownEditor = ArgumentEditorFactory.create(dropdownArg);

        assertTrue(dropdownEditor instanceof DiscreteChoiceEditor);
        assertEquals("0 - 10 (step 5)",
                ArgumentConstraintDescription.describe(dropdownArg.getTypeDefinition()));

        @SuppressWarnings("unchecked")
        JComboBox<Number> dropdown = (JComboBox<Number>) dropdownEditor.getComponent();
        assertEquals(3, dropdown.getItemCount());
        assertEquals(0, dropdown.getItemAt(0));
        assertEquals(5, dropdown.getItemAt(1));
        assertEquals(10, dropdown.getItemAt(2));

        dropdownEditor.setValue(7);
        assertEquals(5, dropdownEditor.getValue());
        dropdownEditor.setValue(8);
        assertEquals(10, dropdownEditor.getValue());

        ArgumentDefinition twentyStepArg = new ArgumentDefinition("step",
                TypeDefinition.integer(ArgumentConstraint.discreteRange(0, 95, 5)), 0);
        assertTrue(ArgumentEditorFactory.create(twentyStepArg) instanceof DiscreteChoiceEditor);

        ArgumentDefinition textFieldArg = new ArgumentDefinition("step",
                TypeDefinition.integer(ArgumentConstraint.discreteRange(0, 100, 5)), 25);
        ArgumentValueEditor textFieldEditor = ArgumentEditorFactory.create(textFieldArg);
        assertTrue(textFieldEditor instanceof NumberFieldEditor);

        JTextField field = (JTextField) textFieldEditor.getComponent();
        field.setText("7");
        assertEquals(5, textFieldEditor.getValue());
        field.setText("8");
        assertEquals(10, textFieldEditor.getValue());
        field.setText("250");
        assertEquals(100, textFieldEditor.getValue());
        textFieldEditor.setValue(27);
        assertEquals(25, textFieldEditor.getValue());
    }

    @Test
    void dropdownSnapsOffGridNumericValuesToNearestChoice() {
        ArgumentDefinition enumArg = new ArgumentDefinition("multiplier",
                TypeDefinition.integer(ArgumentConstraint.enumValues(List.of(1, 2, 3, 5, 8, 13))),
                4);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(enumArg);

        assertTrue(editor instanceof DiscreteChoiceEditor);
        editor.setValue(4);
        assertEquals(5, editor.getValue());
    }

    @Test
    void enumStringConstraintUsesAllowedValuesAndRetainsLoadedValue() {
        ArgumentDefinition argDef =
                new ArgumentDefinition("color",
                        TypeDefinition.string(
                                ArgumentConstraint.enumValues(List.of("red", "green", "blue"))),
                        "red");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof EnumEditor);
        assertEquals("string", StructuredText.describeStructuredShape(argDef.getTypeDefinition()));
        assertEquals("Enum", ArgumentConstraintDescription.describe(argDef.getTypeDefinition()));

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue("green");
        assertEquals("green", editor.getValue());

        editor.setValue("purple");
        assertEquals(4, comboBox.getItemCount());
        assertEquals("purple", editor.getValue());
    }

    @Test
    void numericEnumConstraintUsesAllowedValuesAsChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("multiplier",
                TypeDefinition.doubleType(ArgumentConstraint.enumValues(List.of(0.5, 1.0, 2.0))),
                1.0);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof DiscreteChoiceEditor);
        assertEquals("double", StructuredText.describeStructuredShape(argDef.getTypeDefinition()));
        assertEquals("Enum", ArgumentConstraintDescription.describe(argDef.getTypeDefinition()));

        @SuppressWarnings("unchecked")
        JComboBox<Number> comboBox = (JComboBox<Number>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue(2.0);
        assertEquals(2.0, editor.getValue());
    }

    @Test
    void structuredArgumentsUseGridPanelAndDescribeShape() {
        ArgumentDefinition listArg = new ArgumentDefinition("tags",
                TypeDefinition.listOf(TypeDefinition.string()), List.of());
        ArgumentValueEditor listEditor = ArgumentEditorFactory.create(listArg);
        assertTrue(listEditor instanceof StructuredGridPanel);
        assertEquals("List of string",
                StructuredText.describeStructuredShape(listArg.getTypeDefinition()));
        listEditor.setValue(List.of("common", "rare"));
        assertEquals(List.of("common", "rare"), listEditor.getValue());

        ArgumentDefinition nestedListArg = new ArgumentDefinition("groups",
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer())), List.of());
        assertEquals("List of List of int",
                StructuredText.describeStructuredShape(nestedListArg.getTypeDefinition()));
        ArgumentValueEditor nestedListEditor = ArgumentEditorFactory.create(nestedListArg);
        nestedListEditor.setValue(List.of(List.of(1, 2), List.of(3)));
        assertEquals(List.of(List.of(1, 2), List.of(3)), nestedListEditor.getValue());

        ArgumentDefinition tableArg = new ArgumentDefinition("weights",
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()),
                Map.of());
        ArgumentValueEditor tableEditor = ArgumentEditorFactory.create(tableArg);
        assertTrue(tableEditor instanceof StructuredGridPanel);
        assertEquals("string \u2192 int",
                StructuredText.describeStructuredShape(tableArg.getTypeDefinition()));
        tableEditor.setValue(Map.of("fire", 10));
        assertEquals(Map.of("fire", 10), tableEditor.getValue());
    }

    @Test
    void missingEnumSupportFallsBackToReadOnlyEditor() {
        ArgumentDefinition missingEnumArg = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("MissingEnum"), "WARRIOR");
        ArgumentValueEditor missingEnumEditor = ArgumentEditorFactory.create(missingEnumArg);
        assertTrue(missingEnumEditor instanceof UnsupportedValueEditor);
        missingEnumEditor.setEditable(true);
        assertTrue(!((JTextField) missingEnumEditor.getComponent()).isEditable());

        ArgumentDefinition unregisteredEnumArg = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("EntityType"), "WARRIOR");
        assertTrue(ArgumentEditorFactory
                .create(unregisteredEnumArg) instanceof UnsupportedValueEditor);
    }

    @Test
    void anyStringConstraintUsesFreeEntryTextField() {
        ArgumentDefinition argDef =
                new ArgumentDefinition("label", TypeDefinition.string(), "default");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof StringFieldEditor);
        assertEquals("string", StructuredText.describeStructuredShape(argDef.getTypeDefinition()));
        assertEquals("", ArgumentConstraintDescription.describe(argDef.getTypeDefinition()));

        editor.setValue("hello");
        assertEquals("hello", editor.getValue());
    }

    @Test
    void booleanConstraintUsesAnyAndAllowsBothValues() {
        ArgumentDefinition anyBool = new ArgumentDefinition("flag", TypeDefinition.bool(), true);
        ArgumentValueEditor anyBoolEditor = ArgumentEditorFactory.create(anyBool);
        assertTrue(anyBoolEditor instanceof EnumEditor);
        assertEquals("", ArgumentConstraintDescription.describe(anyBool.getTypeDefinition()));

        @SuppressWarnings("unchecked")
        JComboBox<Object> anyBoolCombo = (JComboBox<Object>) anyBoolEditor.getComponent();
        assertEquals(2, anyBoolCombo.getItemCount());
        anyBoolEditor.setValue(false);
        assertEquals(false, anyBoolEditor.getValue());

        ArgumentDefinition restrictedBool = new ArgumentDefinition("flag",
                TypeDefinition.bool(ArgumentConstraint.enumValues(List.of(false))), false);
        ArgumentValueEditor restrictedBoolEditor = ArgumentEditorFactory.create(restrictedBool);
        assertTrue(restrictedBoolEditor instanceof EnumEditor);
        assertEquals("",
                ArgumentConstraintDescription.describe(restrictedBool.getTypeDefinition()));

        @SuppressWarnings("unchecked")
        JComboBox<Object> restrictedBoolCombo =
                (JComboBox<Object>) restrictedBoolEditor.getComponent();
        assertEquals(2, restrictedBoolCombo.getItemCount());
        restrictedBoolEditor.setValue(true);
        assertEquals(true, restrictedBoolEditor.getValue());
    }

    @Test
    void enumBaseTypeUsesProviderValuesAsChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("EntityType"), "WARRIOR");

        assertEquals("EntityType",
                StructuredText.describeStructuredShape(argDef.getTypeDefinition()));

        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef, enumProvider(
                name -> "EntityType".equals(name) ? List.of("WARRIOR", "MAGE", "ROGUE") : null));

        assertTrue(editor instanceof EnumEditor);

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue("MAGE");
        assertEquals("MAGE", editor.getValue());
    }

    @Test
    void tableConstraintDescriptionShowsNestedConstraints() {
        ArgumentDefinition typeWeights = new ArgumentDefinition("typeWeights",
                TypeDefinition.tableOf(TypeDefinition.enumType("CardType"),
                        TypeDefinition.integer(ArgumentConstraint.range(0, 10))),
                Map.of());
        assertEquals("Enum \u2192 0 - 10",
                ArgumentConstraintDescription.describe(typeWeights.getTypeDefinition()));

        ArgumentDefinition caps =
                new ArgumentDefinition("caps",
                        TypeDefinition.tableOf(TypeDefinition.string(),
                                TypeDefinition.integer(ArgumentConstraint.range(1, 999))),
                        Map.of());
        assertEquals("none \u2192 1 - 999",
                ArgumentConstraintDescription.describe(caps.getTypeDefinition()));

        ArgumentDefinition poolsByType = new ArgumentDefinition("poolsByType",
                TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition
                                .listOf(TypeDefinition.integer(ArgumentConstraint.range(1, 100)))),
                Map.of());
        assertEquals("none \u2192 1 - 100",
                ArgumentConstraintDescription.describe(poolsByType.getTypeDefinition()));

        ArgumentDefinition enumArg = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("EntityType"), "WARRIOR");
        assertEquals("Enum", ArgumentConstraintDescription.describe(enumArg.getTypeDefinition()));
    }

    @Test
    void warnsForBooleanAndStringConstraintsThatAreIgnored() {
        Module module = testModule(List.of(
                new ArgumentDefinition("enumBool",
                        TypeDefinition.bool(ArgumentConstraint.enumValues(List.of(false))), false),
                new ArgumentDefinition("badString",
                        TypeDefinition.string(ArgumentConstraint.range(1, 5)), "a")));

        IssueTracker.clear();
        ArgumentConstraintDescription.checkModuleConstraints(module);

        assertTrue(IssueTracker.hasWarnings());
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("enumBool")));
        assertTrue(IssueTracker.getWarnings().stream().anyMatch(w -> w.contains("badString")));
    }

    @Test
    void doesNotWarnForSupportedConstraints() {
        Module module =
                testModule(List.of(new ArgumentDefinition("flag", TypeDefinition.bool(), true),
                        new ArgumentDefinition("label", TypeDefinition.string(), "a"),
                        new ArgumentDefinition("color",
                                TypeDefinition.string(
                                        ArgumentConstraint.enumValues(List.of("red", "blue"))),
                                "red")));

        IssueTracker.clear();
        ArgumentConstraintDescription.checkModuleConstraints(module);

        assertFalse(IssueTracker.hasWarnings());
    }

    private static Module testModule(List<ArgumentDefinition> arguments) {
        return new Module("test_module", "Test Module", "", Set.of("dev"), arguments,
                new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        return LuaValue.NIL;
                    }
                }, null, "test.lua", 0, false, false, null, "author", "1.0", Map.of(), null, null, null, null, null);
    }

    private static EnumValuesProvider enumProvider(
            java.util.function.Function<String, List<String>> valuesByName) {
        return new EnumValuesProvider() {
            @Override
            public List<String> getEnumValues(String enumName) {
                return valuesByName.apply(enumName);
            }

            @Override
            public String getEnumValueDisplayName(String enumName, String canonicalValue) {
                return canonicalValue;
            }
        };
    }

    private static void simulateFocusLost(JTextField field) {
        for (FocusListener listener : field.getFocusListeners()) {
            listener.focusLost(new FocusEvent(field, FocusEvent.FOCUS_LOST));
        }
    }
}
