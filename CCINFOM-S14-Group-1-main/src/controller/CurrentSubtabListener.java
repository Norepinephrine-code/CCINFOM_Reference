package src.controller;

import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import src.view.gui.TopView;

/**
 * CurrentSubtabListener monitors the user's current Subtab.
 */
public class CurrentSubtabListener implements ChangeListener {
    private TopView topView;

    public CurrentSubtabListener(TopView topView) {
        this.topView = topView;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int index = ((SingleSelectionModel) e.getSource()).getSelectedIndex();
        topView.setCurrentSubtabName(index);
        System.out.println(topView.getCurrentTabName() + "/" + topView.getCurrentSubtabName());
        topView.resetFields(topView.getCurrentTabName(), topView.getCurrentSubtabName());
    }
}
