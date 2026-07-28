package redactedrice.ptcgr.randomizer.gui.moduleconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import redactedrice.randomizer.lua.arguments.ArgumentConstraint;
import redactedrice.randomizer.lua.arguments.ArgumentDefinition;
import redactedrice.randomizer.lua.arguments.TypeDefinition;

class ArgumentEditorFactoryTest {

    @Test
    void anyIntegerConstraintUsesFullIntRange() {
        ArgumentDefinition argDef = new ArgumentDefinition("numMoves", TypeDefinition.integer(), 2);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof NumberFieldEditor);
        assertEquals("Int min - Int max", ArgumentEditorFactory.describeConstraint(argDef));

        JTextField field = (JTextField) editor.getComponent();
        field.setText("12345");
        assertEquals(12345, editor.getValue());
    }

    @Test
    void integerRangeShowsSymbolicIntBoundsWhenMatched() {
        ArgumentDefinition fullRange =
                new ArgumentDefinition("full", TypeDefinition.integer(), 0);
        assertEquals("Int min - Int max", ArgumentEditorFactory.describeConstraint(fullRange));

        ArgumentDefinition zeroToMax = new ArgumentDefinition("zeroToMax",
                TypeDefinition.integer(ArgumentConstraint.range(0, Integer.MAX_VALUE)), 0);
        assertEquals("0 - Int max", ArgumentEditorFactory.describeConstraint(zeroToMax));

        ArgumentDefinition minToHundred = new ArgumentDefinition("minToHundred",
                TypeDefinition.integer(ArgumentConstraint.range(Integer.MIN_VALUE, 100)), 0);
        assertEquals("Int min - 100", ArgumentEditorFactory.describeConstraint(minToHundred));
    }

    @Test
    void anyIntegerConstraintCapsToIntRangeOnFocusLoss() {
        ArgumentDefinition argDef = new ArgumentDefinition("anyInt", TypeDefinition.integer(), 0);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);
        JTextField field = (JTextField) editor.getComponent();

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
    void rangeConstraintCapsOutOfRangeValuesOnFocusLoss() {
        ArgumentDefinition argDef = new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof NumberFieldEditor);
        assertEquals("0 - 2", ArgumentEditorFactory.describeConstraint(argDef));

        JTextField field = (JTextField) editor.getComponent();
        field.setText("2");
        assertEquals(2, editor.getValue());

        // Above max: capped to max as soon as the field loses focus, not rejected at save time
        field.setText("5");
        simulateFocusLost(field);
        assertEquals("2", field.getText());
        assertEquals(2, editor.getValue());

        // Below min: capped to min the same way
        field.setText("-3");
        simulateFocusLost(field);
        assertEquals("0", field.getText());
        assertEquals(0, editor.getValue());
    }

    @Test
    void rangeConstraintCapsOutOfRangeValuesInGetValueAsSafetyNet() {
        ArgumentDefinition argDef = new ArgumentDefinition("numMoves",
                TypeDefinition.integer(ArgumentConstraint.range(0, 2)), 2);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        JTextField field = (JTextField) editor.getComponent();
        field.setText("10");
        // getValue() also caps, even if focusLost never fired (e.g. saved without ever
        // leaving the field)
        assertEquals(2, editor.getValue());
    }

    private static void simulateFocusLost(JTextField field) {
        for (FocusListener listener : field.getFocusListeners()) {
            listener.focusLost(new FocusEvent(field, FocusEvent.FOCUS_LOST));
        }
    }

    @Test
    void discreteRangeConstraintPrepopulatesChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("step",
                TypeDefinition.integer(ArgumentConstraint.discreteRange(0, 10, 5)), 0);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof DiscreteChoiceEditor);
        assertEquals("0 - 10 (step 5)", ArgumentEditorFactory.describeConstraint(argDef));

        @SuppressWarnings("unchecked")
        JComboBox<Number> comboBox = (JComboBox<Number>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());
        assertEquals(0, comboBox.getItemAt(0));
        assertEquals(5, comboBox.getItemAt(1));
        assertEquals(10, comboBox.getItemAt(2));

        editor.setValue(5);
        assertEquals(5, editor.getValue());
    }

    @Test
    void enumConstraintUsesAllowedValuesAsChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("multiplier",
                TypeDefinition.doubleType(ArgumentConstraint.enumValues(List.of(0.5, 1.0, 2.0))),
                1.0);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof DiscreteChoiceEditor);
        assertEquals("custom enum", ArgumentEditorFactory.describeConstraint(argDef));

        @SuppressWarnings("unchecked")
        JComboBox<Number> comboBox = (JComboBox<Number>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue(2.0);
        assertEquals(2.0, editor.getValue());
    }

    @Test
    void enumBaseTypeShowsRegisteredEnumName() {
        ArgumentDefinition argDef = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("EntityType"), "WARRIOR");

        assertEquals("EntityType", ArgumentEditorFactory.describeConstraint(argDef));
    }

    @Test
    void seedOffsetConstraintIsBlank() {
        assertEquals("", ArgumentEditorFactory.describeSeedOffset());
    }

    @Test
    void listArgumentUsesStructuredGridPanel() {
        ArgumentDefinition argDef = new ArgumentDefinition("tags",
                TypeDefinition.listOf(TypeDefinition.string()), List.of());
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof StructuredGridPanel);
        assertEquals("List of string", ArgumentEditorFactory.describeConstraint(argDef));

        editor.setValue(List.of("common", "rare"));
        assertEquals(List.of("common", "rare"), editor.getValue());
    }

    @Test
    void nestedListOfListDescribesShapeRecursively() {
        ArgumentDefinition argDef = new ArgumentDefinition("groups",
                TypeDefinition.listOf(TypeDefinition.listOf(TypeDefinition.integer())), List.of());

        assertEquals("List of List of int", ArgumentEditorFactory.describeConstraint(argDef));

        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);
        editor.setValue(List.of(List.of(1, 2), List.of(3)));
        assertEquals(List.of(List.of(1, 2), List.of(3)), editor.getValue());
    }

    @Test
    void tableArgumentUsesStructuredGridPanel() {
        ArgumentDefinition argDef = new ArgumentDefinition("weights",
                TypeDefinition.tableOf(TypeDefinition.string(), TypeDefinition.integer()),
                Map.of());
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof StructuredGridPanel);
        assertEquals("string \u2192 int", ArgumentEditorFactory.describeConstraint(argDef));

        editor.setValue(Map.of("fire", 10));
        assertEquals(Map.of("fire", 10), editor.getValue());
    }

    @Test
    void nestedTableOfListsDescribesShapeRecursively() {
        ArgumentDefinition argDef = new ArgumentDefinition("poolsByType",
                TypeDefinition.tableOf(TypeDefinition.string(),
                        TypeDefinition.listOf(TypeDefinition.integer())),
                Map.of());

        assertEquals("string \u2192 List of int",
                ArgumentEditorFactory.describeConstraint(argDef));
    }

    @Test
    void unsupportedBaseTypeFallsBackToReadOnlyEditor() {
        ArgumentDefinition argDef = new ArgumentDefinition("entityType",
                TypeDefinition.enumType("MissingEnum"), "WARRIOR");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof UnsupportedValueEditor);

        // Unsupported editors stay read-only regardless of the requested editable state
        editor.setEditable(true);
        assertTrue(editor.getComponent() instanceof JTextField);
        assertTrue(!((JTextField) editor.getComponent()).isEditable());
    }

    @Test
    void anyStringConstraintUsesFreeEntryTextField() {
        ArgumentDefinition argDef =
                new ArgumentDefinition("label", TypeDefinition.string(), "default");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof StringFieldEditor);
        assertEquals("", ArgumentEditorFactory.describeConstraint(argDef));

        editor.setValue("hello");
        assertEquals("hello", editor.getValue());
    }

    @Test
    void enumStringConstraintUsesAllowedValuesAsChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("color",
                TypeDefinition.string(ArgumentConstraint.enumValues(List.of("red", "green", "blue"))),
                "red");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof EnumEditor);
        assertEquals("custom enum", ArgumentEditorFactory.describeConstraint(argDef));

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue("green");
        assertEquals("green", editor.getValue());
    }

    @Test
    void anyBooleanConstraintUsesTrueFalseChoices() {
        ArgumentDefinition argDef = new ArgumentDefinition("flag", TypeDefinition.bool(), true);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof EnumEditor);
        assertEquals("true / false", ArgumentEditorFactory.describeConstraint(argDef));

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(2, comboBox.getItemCount());

        editor.setValue(false);
        assertEquals(false, editor.getValue());
    }

    @Test
    void booleanConstraintAlwaysShowsBothTrueAndFalseChoices() {
        // Even if the argument definition restricts the enum to a single allowed value, a
        // boolean "choice" of just one value doesn't make sense - the UI always offers both.
        ArgumentDefinition argDef = new ArgumentDefinition("flag",
                TypeDefinition.bool(ArgumentConstraint.enumValues(List.of(false))), false);
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof EnumEditor);
        assertEquals("true / false", ArgumentEditorFactory.describeConstraint(argDef));

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(2, comboBox.getItemCount());

        editor.setValue(true);
        assertEquals(true, editor.getValue());
    }

    @Test
    void enumBaseTypeUsesProviderValuesAsChoices() {
        ArgumentDefinition argDef =
                new ArgumentDefinition("entityType", TypeDefinition.enumType("EntityType"), "WARRIOR");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef,
                name -> "EntityType".equals(name) ? List.of("WARRIOR", "MAGE", "ROGUE") : null);

        assertTrue(editor instanceof EnumEditor);

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox = (JComboBox<Object>) editor.getComponent();
        assertEquals(3, comboBox.getItemCount());

        editor.setValue("MAGE");
        assertEquals("MAGE", editor.getValue());
    }

    @Test
    void enumBaseTypeWithoutProviderFallsBackToReadOnlyEditor() {
        ArgumentDefinition argDef =
                new ArgumentDefinition("entityType", TypeDefinition.enumType("EntityType"), "WARRIOR");
        ArgumentValueEditor editor = ArgumentEditorFactory.create(argDef);

        assertTrue(editor instanceof UnsupportedValueEditor);
    }
}
