package src.view.gui;

import javax.swing.JPanel;

/**
 * A named panel is a JPanel that also has a name -- useful for implementing
 * Tabs and Subtabs.
 * 
 */
public abstract class NamedPanel extends JPanel {
    public abstract String getName();
}
