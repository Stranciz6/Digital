package de.neemann.digital.data;

import de.neemann.digital.core.Observer;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * The table model to represent a value table.
 * <p>
 * Created by hneemann on 24.08.16.
 */
public class ValueTableModel implements TableModel, Observer {

    private final ValueTable values;
    private ArrayList<TableModelListener> listeners;

    /**
     * Creates a new table model
     *
     * @param values the values to wrap
     */
    public ValueTableModel(ValueTable values) {
        this.values = values;
        listeners = new ArrayList<>();
        values.addObserver(this);
    }

    @Override
    public int getRowCount() {
        return values.getRows();
    }

    @Override
    public int getColumnCount() {
        return values.getColumns() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return Lang.get("number");
        else
            return values.getColumnName(columnIndex - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return Integer.class;
        else
            return Value.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return rowIndex;
        else
            return values.getValue(rowIndex, columnIndex - 1);
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void hasChanged() {
        SwingUtilities.invokeLater(() -> {
            TableModelEvent tme = new TableModelEvent(this);
            for (TableModelListener l : listeners)
                l.tableChanged(tme);
        });
    }
}
