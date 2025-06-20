package redactedrice.ptcgr.randomizer.gui.dualselector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

public class CategoryChangedListener implements ActionListener 
{
	private ActionsListTableModel toUpdate;
	
	public CategoryChangedListener(ActionsListTableModel toUpdate)
	{
		this.toUpdate = toUpdate;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() instanceof JComboBox)
		{
			toUpdate.setRowsByCategory((String)((JComboBox<?>)e.getSource()).getSelectedItem());
		}
	}
}
