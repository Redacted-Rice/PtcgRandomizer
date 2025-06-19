package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import redactedrice.ptcgr.randomizer.actions.Action;

// TODO: Split between tables?
public class TableModelAction extends AbstractTableModel
{
	static public enum Columns {
	    ID(0),
	    ACTION(1),
	    DESCRIPTION(2),
	    CONFIG(3);

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
			"Id", "Action", "Description", "Config"
	};
	public static final int DESCRIPTION_COLUMN = 2;
	public static final int CONFIG_COLUMN = 3;
	
	List<Action> data;
	
	public TableModelAction()
	{
		data = new ArrayList<>();
		data.add(null);
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public int getColumnCount() 
	{
		return COLUMN_HEADERS.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		if (data.get(rowIndex) != null)
		{
			switch (columnIndex)
			{
				case 0: return data.get(rowIndex).getId();
				case 1: return data.get(rowIndex).getName();
				case 2: return data.get(rowIndex).getDescription();
				case CONFIG_COLUMN: return data.get(rowIndex).numConfigs();
				default: return "ERROR";
			}
		}
		else
		{
			return "";
		}
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
    
    public Action getRow(int index)
    {
    	return data.get(index);
    }
	
	public List<Action> getRows()
	{
		return data.subList(0, data.size() - 1);
	}
	
	public void removeRow(int index)
	{
		// If its not the end null, remove it
		if (data.get(index) != null)
		{
			data.remove(index);
	        fireTableRowsDeleted(index, index);
		}
	}
	
	public void removeRows(int[] indecies)
	{
		if (indecies.length > 0)
		{
			Arrays.sort(indecies); 
			for (int i = indecies.length - 1; i >=0; i--)
			{
				// If its not the end null, remove it
				if (data.get(i) != null)
				{
					data.remove(indecies[i]);
				}
			}
			fireTableRowsDeleted(indecies[0], indecies[indecies.length - 1]);
		}
	}
	
	public void insertRow(int index, Action a)
	{
		// if its the null index or further, set it to the last index
		if (index >= data.size())
		{
			index = data.size() - 1;
		}
		data.add(index, a);
        fireTableRowsInserted(index, index);
	}
	
	public void addRow(Action a)
	{
		insertRow(0, a);
	}
}
