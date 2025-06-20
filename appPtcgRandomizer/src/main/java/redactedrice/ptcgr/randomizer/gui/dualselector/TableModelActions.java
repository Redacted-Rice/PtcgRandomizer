package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import redactedrice.ptcgr.randomizer.actions.Action;

// TODO: Split between tables?
public class TableModelActions extends AbstractTableModel
{
	static public enum Columns {
	    NAME(0),
	    CONFIG(1);

	    private final int value;

	    Columns(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	        return value;
	    }

	    // Optional: reverse lookup
	    public static Columns fromValue(int value) {
	        for (Columns column : values()) {
	            if (column.value == value) {
	                return column;
	            }
	        }
	        throw new IllegalArgumentException("Unknown Column value: " + value);
	    }
	}
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] COLUMN_HEADERS = {
			"Action", "Config"
	};
	
	List<Action> data;
	
	public TableModelActions()
	{
		data = new ArrayList<>();
	}

	@Override
	public int getRowCount()
	{
		// This needs to include the null row
		return data.size();
	}
	
	public int getDataRowCount()
	{
		return getRowCount();
	}

	@Override
	public int getColumnCount() 
	{
		return COLUMN_HEADERS.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		if (rowIndex < getDataRowCount())
		{
			switch (Columns.fromValue(columnIndex))
			{
				case NAME: return data.get(rowIndex).getName();
				case CONFIG: return data.get(rowIndex).numConfigs();
				default: return "";
			}
		}
		return "";
	}
	
	@Override
    public Class<?> getColumnClass(int column) 
	{
    	if (column < COLUMN_HEADERS.length && column >= 0) 
    	{
    		return String.class;
    	}
        return Object.class;
    }
	
    @Override
    public String getColumnName(int column) 
    {
    	if (column < COLUMN_HEADERS.length && column >= 0) 
    	{
    		return COLUMN_HEADERS[column];
    	}
    	return null;
    }
	
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == Columns.CONFIG.getValue(); // Enable editing for the button column
    }
    
    public Action getRow(int index)
    {
		if (index < getDataRowCount())
		{
			return data.get(index);
		}
		return null;
    }
	
	public List<Action> getRows()
	{
		return data.subList(0, getDataRowCount());
	}

	public String getRowDescription(int row) {
		if (row < getDataRowCount()) {
			return getRow(row).getDescription();
		}
		return "";
	}
	
	public Action removeRow(int index)
	{
		// Guard removing the last empty row
		if (index < getDataRowCount())
		{
			Action a = data.remove(index);
	        fireTableRowsDeleted(index, index);
	        return a;
		}
		return null;
	}
	
	public void removeRows(int[] indecies)
	{
		if (indecies.length > 0)
		{
			Arrays.sort(indecies); 
			for (int i = indecies.length - 1; i >=0; i--)
			{
				if (indecies[i] < getDataRowCount())
				{
					data.remove(indecies[i]);
				} 
			}
	        fireTableDataChanged();
		}
	}
	
	public void insertRow(int index, Action a)
	{
		if (a != null) {
			if (index >= getDataRowCount())
			{
				index = getDataRowCount();
			}
			data.add(index, a);
	        fireTableRowsInserted(index, index);
		}
	}
	
	public void appendRow(Action a)
	{
		insertRow(getDataRowCount(), a);
	}
	
    public void reorderRow(int from, int to) {
        if (from != to && from < getDataRowCount()) {
            Action obj = removeRow(from);
            insertRow(to, obj);
            fireTableDataChanged();
        }
    }
    
    public void reorderRows(List<Integer> fromIndices, int toIndex) {
        List<Action> moving = new ArrayList<>();
        // Get rows before shifting indices
        for (int i = 0; i < fromIndices.size(); i++) {
        	if (i >= getDataRowCount()) {
        		fromIndices.remove(i);
        		i--;
        	} else {
        		moving.add(getRow(fromIndices.get(i)));
        	}
        }

        // Remove from highest to lowest to avoid shifting
        fromIndices.stream().sorted(Comparator.reverseOrder()).forEach(i -> removeRow((int) i));

        // Adjust drop index if rows were above it
        long countAbove = fromIndices.stream().filter(i -> i < toIndex).count();
        int adjustedIndex = toIndex - (int) countAbove;
        for (int i = moving.size() - 1; i >= 0; i--) {
        	insertRow(adjustedIndex, moving.get(i));
        }
        fireTableDataChanged();
    }
}
