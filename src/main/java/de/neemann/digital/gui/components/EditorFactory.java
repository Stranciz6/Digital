package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;
import de.process.utils.gui.ErrorMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author hneemann
 */
public final class EditorFactory {

    public static final EditorFactory INSTANCE = new EditorFactory();
    private HashMap<Class<?>, Class<? extends Editor>> map = new HashMap<>();

    private EditorFactory() {
        add(String.class, StringEditor.class);
        add(Integer.class, IntegerEditor.class);
        add(Color.class, ColorEditor.class);
        add(Boolean.class, BooleanEditor.class);
        add(DataField.class, DataFieldEditor.class);
    }

    public <T> void add(Class<T> clazz, Class<? extends Editor<T>> editor) {
        map.put(clazz, editor);
    }

    public <T> Editor<T> create(Class<T> clazz, T value) {
        Class<? extends Editor> fac = map.get(clazz);
        if (fac == null)
            throw new RuntimeException("no editor found for " + clazz.getSimpleName());

        try {
            Constructor<? extends Editor> c = fac.getConstructor(value.getClass());
            return c.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("error creating editor", e);
        }
    }

    private static class StringEditor implements Editor<String> {

        private final JTextField text;

        public StringEditor(String value) {
            text = new JTextField(10);
            text.setText(value);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return text;
        }

        @Override
        public String getValue() {
            return text.getText();
        }
    }

    private static class IntegerEditor implements Editor<Integer> {
        private final JComboBox<Integer> comboBox;

        public IntegerEditor(Integer value) {
            comboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Integer getValue() {
            Object item = comboBox.getSelectedItem();
            if (item instanceof Number)
                return ((Number) item).intValue();
            else {
                return Integer.decode(item.toString());
            }
        }
    }

    private static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value) {
            bool = new JCheckBox("", value);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return bool;
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }
    }


    private static class ColorEditor implements Editor<Color> {

        private Color color;
        private final JButton button;

        public ColorEditor(Color value) {
            this.color = value;
            button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color col = JColorChooser.showDialog(button, Lang.get("msg_color"), color);
                    if (col != null) {
                        color = col;
                        button.setBackground(color);
                    }
                }
            });
            button.setBackground(color);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return button;
        }

        @Override
        public Color getValue() {
            return color;
        }
    }

    private static class DataFieldEditor implements Editor<DataField> {

        private DataField data;

        public DataFieldEditor(DataField data) {
            this.data = data;
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JButton(new AbstractAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DataEditor de = new DataEditor(panel, data, attr);
                    if (de.showDialog()) {
                        data = de.getDataField();
                    }
                }
            }));
            panel.add(new JButton(new AbstractAction(Lang.get("btn_load")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        try {
                            data = new DataField(fc.getSelectedFile());
                        } catch (IOException e1) {
                            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(panel);
                        }
                    }
                }
            }));
            return panel;
        }

        @Override
        public DataField getValue() {
            return data.getMinimized();
        }
    }
}
