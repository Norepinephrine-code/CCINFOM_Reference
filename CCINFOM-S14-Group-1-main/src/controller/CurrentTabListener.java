package src.controller;

import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import src.view.gui.TopView;

/**
 * CurrentTabListener monitors the user's current tab.
 */
public class CurrentTabListener implements ChangeListener {
    private TopView topView;

    public CurrentTabListener(TopView topView) {
        this.topView = topView;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int index = ((SingleSelectionModel) e.getSource()).getSelectedIndex();
        topView.setCurrentTabName(index);
        System.out.println(topView.getCurrentTabName() + "/" + topView.getCurrentSubtabName());
        topView.resetFields(topView.getCurrentTabName(), topView.getCurrentSubtabName());
    }
}
