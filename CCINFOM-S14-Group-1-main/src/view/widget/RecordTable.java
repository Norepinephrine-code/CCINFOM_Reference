package src.view.widget;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;

import src.controller.RecordTableListener;

public class RecordTable extends JDialog {
    private JTable table;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JButton button;
    private GridBagConstraints c = new GridBagConstraints();
    private String[] shownColumnNames;
    private String[] columnNames;

    public RecordTable(JFrame frame, String recordName, String noun, String... shownColumnNames) {
        super(frame, "Please select " + noun, true);
        this.shownColumnNames = shownColumnNames;
        this.setSize(WidgetFactory.RECORD_TABLE_SIZE);
        this.setLocationRelativeTo(frame);
        this.setResizable(true);
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(WidgetFactory.createJLabel("Please select " + noun + ":",
                WidgetFactory.Fonts.SUBTITLE), c);
        c.gridy++;
    }

    /**
     * Hide certain columns from the user's view based of the shownColumnNames
     * String array passed in on initialization.
     */
    public void hideColumns() {
        TableColumnModel columnModel = table.getColumnModel();
        boolean show;
        for (int i = columnNames.length - 1; i >= 0; i--) {
            show = false;
            for (int j = 0; j < shownColumnNames.length; j++) {
                if (shownColumnNames[j].equals(columnNames[i])) {
                    System.out.println("\tThis column must be shown.");
                    show = true;
                    break;
                }
            }
            if (!show) {
                TableColumn hiddenColumn = columnModel.getColumn(i);
                columnModel.removeColumn(hiddenColumn);
                System.out.println("\tHiding column " + columnNames[i] + " - Index " + i);
            }
        }
    }

    /**
     * Initialize data to this RecordTable. This must only be called once, when
     * setting data for the first time, as this also sets up the table.
     * 
     * @param data
     * @param columnNames
     */
    public void initializeData(String[][] data, String[] columnNames) {
        this.columnNames = columnNames;
        this.table = WidgetFactory.createJTable(data, columnNames);
        this.hideColumns();
        this.scrollPane = WidgetFactory.createJScrollPane(table);

        c.weightx = 1;
        c.weighty = 1;
        panel.add(scrollPane, c);
        c.gridy++;

        c.weightx = 0;
        c.weighty = 0;
        button = WidgetFactory.createJButton("Select");
        panel.add(button, c);
        c.gridy++;

        this.add(panel);
    }

    /**
     * Set data to this RecordTable. Whereas {@link #initializeData()} can only be
     * called once, this can be called multiple times in order to update the values
     * in the table view.
     * 
     * @param data
     * @param columnNames
     */
    public void setData(String[][] data, String[] columnNames) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (model.getRowCount() < data.length) {
            model.addRow(data[data.length - 1]);
        }
        model.setDataVector(data, columnNames);
        this.hideColumns();
    }

    /**
     * Add a RecordTableListener to this RecordTable.
     * 
     * @param listener
     */
    public void setListener(RecordTableListener listener) {
        button.addActionListener(listener);
    }

    /**
     * Get the currently-selected row index.
     * 
     * @return
     */
    public int getSelected() {
        return table.getSelectedRow();
    }

    /**
     * Get data corresponding to a certain row in the table.
     * 
     * @param row     row index
     * @param columns amount of columns
     * @return a hashmap mapping column names to corresponding values
     */
    public HashMap<String, String> getRowData(int row, int columns) {
        HashMap<String, String> data = new HashMap<>();
        for (int column = 0; column < columns; column++) {
            String key = table.getModel().getColumnName(column);
            String value = table.getModel().getValueAt(row, column).toString();
            data.put(key, value);
            System.out.printf("%s : %s\n", key, value);
        }
        return data;
    }

    /**
     * Get the data of the currently-selected row.
     * 
     * @return
     */
    public HashMap<String, String> getSelectedRowData() {
        int row = table.getSelectedRow();
        int columns = table.getModel().getColumnCount();
        return getRowData(row, columns);
    }

    /**
     * Get the data of the last row. Since we sort by ID, this returns the "newest"
     * row in a record.
     * 
     * @return
     */
    public HashMap<String, String> getLastRowData() {
        int row = table.getModel().getRowCount() - 1;
        int columns = table.getModel().getColumnCount();
        return getRowData(row, columns);
    }
}
