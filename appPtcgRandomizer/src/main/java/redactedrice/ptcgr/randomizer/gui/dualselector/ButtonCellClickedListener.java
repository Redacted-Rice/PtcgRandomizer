package redactedrice.ptcgr.randomizer.gui.dualselector;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import redactedrice.ptcgr.randomizer.actions.Action;

public class ButtonCellClickedListener implements ActionListener {
    public ButtonCellClickedListener(Action action) {
        actionPerformed(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame = new JFrame("New Window");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JLabel("Hello, World!", SwingConstants.CENTER));
        frame.setVisible(true);
    }
}
