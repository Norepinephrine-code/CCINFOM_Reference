package src.view.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import src.model.Genre;
import src.model.StaffDepartment;
import src.model.UserRegion;
import src.view.widget.WidgetFactory;

public class Subtab extends NamedPanel {
    private ArrayList<ArrayList<JComponent>> components;
    private HashMap<String, JComponent> componentName;
    private HashMap<String, JComponent> componentDbLinks;
    private String name;

    private static final String JSON_PATH_PREFIX = "src/view/gui/";

    public Subtab(String name, String jsonPathString) {
        this.name = name;
        this.components = new ArrayList<>();
        this.componentName = new HashMap<>();
        this.componentDbLinks = new HashMap<>();
        this.setComponents(jsonPathString);
        this.addComponents();
    }

    /**
     * Sets the components ArrayList and the componentName HashMap.
     * 
     * @param jsonPathString path to JSON file from which to add component data
     */
    private void setComponents(String jsonFileName) {
        Path jsonPath = Paths.get(JSON_PATH_PREFIX + jsonFileName);
        System.out.println("\nSetting components from " + JSON_PATH_PREFIX + jsonFileName);
        try {
            String content = new String(Files.readAllBytes(jsonPath));
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                ArrayList<JComponent> rowComponents = new ArrayList<>();
                JSONArray row = jsonArray.getJSONArray(i);

                for (int j = 0; j < row.length(); j++) {
                    JSONObject cell = row.getJSONObject(j);
                    JComponent component = WidgetFactory.componentFromJSON(cell);

                    String name = cell.optString("name");
                    if (!name.isEmpty()) {
                        componentName.put(name, component);
                    }

                    String column = cell.optString("db_link");
                    if (!column.isEmpty()) {
                        System.out.println("\t" + name + " component is associated with the " + column + " column.");
                        if (componentDbLinks.put(column, component) != null) {
                            System.out.println("\t\tAnother component was associated with this column before!");
                        } ;
                    }

                    rowComponents.add(component);
                }

                components.add(rowComponents);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actually adds the components to the Subtab.
     */
    private void addComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(16, 16, 16, 16);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        for (ArrayList<JComponent> row : components) {
            c.gridx = 0;
            for (JComponent component : row) {
                if (component instanceof JButton) {
                    c.fill = GridBagConstraints.NONE;
                } else {
                    c.fill = GridBagConstraints.HORIZONTAL;
                }

                if (component instanceof JScrollPane) {
                    // Allocate more space to a JTextArea
                    // (which is contained in a JScrollPane)
                    c.gridwidth = 3;
                } else {
                    c.gridwidth = 1;
                }
                this.add(component, c);
                c.gridx++;
            }
            c.gridy++;
        }
    }

    /**
     * 
     * @param name
     * @return the component with a matching name, or null if none exists.
     */
    public JComponent getComponent(String name) {
        return componentName.get(name);
    }

    /**
     * Get the component associated with a column of a dataset.
     * 
     * @param columnName
     * @return
     */
    private JComponent getAssociatedComponent(String columnName) {
        return componentDbLinks.get(columnName);
    }

    public void setAssociatedComponent(String columnName, String componentName) {
        componentDbLinks.put(columnName, getComponent(componentName));
    }

    public void setComponentText(String componentName, String text) {
        setComponentText(getComponent(componentName), text);
    }

    /**
     * 
     * @param component
     * @param text
     */
    public void setComponentText(JComponent component, String text) {
        if (component instanceof AbstractButton) {
            ((AbstractButton) component).setText(text);
        } else if (component instanceof JLabel) {
            ((JLabel) component).setText(text);
        } else if (component instanceof JTextField) {
            ((JTextField) component).setText(text);
        } else if (component instanceof JScrollPane) { // which contains a Viewport, which contains a JTextArea
            ((JTextArea) ((JScrollPane) component).getViewport().getComponent(0)).setText(text);
        } else if (component instanceof JTextField) {
            ((JTextField) component).setText(text);
        } else if (component instanceof JComboBox) {
            JComboBox<String> comboBox = (JComboBox<String>) component;
            String genreName = Genre.findName(text);
            String regionName = UserRegion.findName(text);
            if (genreName != null) {
                comboBox.setSelectedItem(genreName);
            } else if (regionName != null) {
                comboBox.setSelectedItem(regionName);
            } else {
                comboBox.setSelectedIndex(0);
            }
        }
    }

    public void setFields(HashMap<String, String> data) {
        for (String key : data.keySet()) {
            JComponent component = this.getAssociatedComponent(key);
            if (component != null) {
                this.setComponentText(component, data.get(key));
                System.out.println(key + " successfully set.");
            } else {
                System.out.println(key + " is not associated with any component.");
            }
        }
    }

    /**
     * Reset the values of all components in this Subtab that are associated with a
     * column.
     */
    public void resetFields() {
        for (Map.Entry<String, JComponent> map : componentName.entrySet()) {
            String name = map.getKey();
            JComponent component = map.getValue();
            switch (name) {
                case "userId":
                case "user2Id":
                case "staffId":
                case "studioId":
                case "animeId":
                    setComponentText(component, "None selected");
                    break;
                case "studioName":
                case "username":
                case "user2name":
                case "anime":
                case "staffFirstName":
                case "staffLastName":
                    setComponentText(component, "");
                default:
                    if (component instanceof JTextField || component instanceof JScrollPane) {
                        setComponentText(component, "");
                    }
            }
        }
    }

    /**
     * Set an ActionListener for a component in this Subtab.
     * 
     * @param name
     * @param listener
     */
    public void setActionListener(String name, ActionListener listener) {
        System.out.println(this.getName() + "/" + name + "<-ActionListener(" + listener + ")");
        ((AbstractButton) getComponent(name)).addActionListener(listener);
    }

    public void setDocumentListener(String name, DocumentListener listener) {
        System.out.println(this.getName() + "/" + name + "<-DocumentListener(" + listener + ")");
        ((JTextComponent) getComponent(name)).getDocument().addDocumentListener(listener);
    }

    public String getName() {
        return name;
    }

    /**
     * Get the text of a certain component.
     * 
     * @param componentName
     * @param comboBoxType  Optional: combo box type -- region, genre.
     * @return
     */
    public String getComponentText(String componentName, String... comboBoxType) {
        JComponent component = getComponent(componentName);
        String componentClass = component.getClass().getSimpleName();
        String retval = new String();
        switch (componentClass) {
            case "JLabel":
                retval = ((JLabel) component).getText();
                break;
            case "JTextField":
                retval = ((JTextField) component).getText();
                break;
            case "JScrollPane":
                retval = ((JTextArea) ((JScrollPane) component).getViewport().getComponent(0)).getText();
                System.out.println("\tGetting text from a text area is untested");
                break;
            case "JComboBox":
                JComboBox<String> comboBox = (JComboBox<String>) component;
                String selection = comboBox.getSelectedItem().toString();
                switch (comboBoxType[0]) {
                    case "region":
                        retval = UserRegion.findCode(selection);
                        break;
                    case "genre":
                        retval = Genre.findCode(selection);
                        break;
                    case "period":
                        retval = selection;
                        break;
                    case "genre_with_none":
                        if (selection.equals("None")) {
                            retval = "None";
                        } else {
                            retval = Genre.findCode(selection);
                        }
                        break;
                    case "recommendations":
                        retval = selection;
                        break;
                    case "years":
                        if(selection.equals("All time")){
                            retval = "0";
                        } else 
                            retval = selection;
                        break;
                    case "department":
                        retval = StaffDepartment.findCode(selection);
                        break;
                    default:
                        System.err.println("Unhandled comboBoxType " + comboBoxType[0]);
                        break;
                }
                break;
            default:
                System.out.println("Unhandled component class " + componentClass
                        + ". Please add a new case in Subtab.getComponentText()!");
                break;
        }
        return retval;
    }

}
