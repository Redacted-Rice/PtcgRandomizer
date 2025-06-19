package redactedrice.ptcgr.randomizer.gui.dualselector;


public class TableModelActionSelected extends TableModelAction
{
	private static final long serialVersionUID = 1L;
	
	public TableModelActionSelected()
	{
		data.add(null);
	}
	
	@Override
	public int getDataRowCount()
	{
		return data.size() - 1;
	}
}
