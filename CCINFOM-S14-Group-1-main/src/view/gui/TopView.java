package src.view.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

import src.controller.CurrentSubtabListener;
import src.controller.CurrentTabListener;
import src.controller.RecordTableListener;
import src.model.Genre;
import src.model.Records;
import src.model.UserRegion;
import src.view.widget.RecordTable;
import src.view.widget.WidgetFactory;
import src.view.widget.WidgetFactory.Fonts;

public class TopView {
    private JFrame frame;
    private JTabbedPane tabs;

    private HashMap<String, Tab> mainTabs;
    private LinkedHashMap<Tab, ArrayList<Subtab>> tabMap; // * LinkedHashMap preserves insertion order

    private HashMap<String, RecordTable> recordTables;

    private ArrayList<String> tabNames;
    private ArrayList<ArrayList<String>> subtabNames;

    public static final String RECORDS_TAB = "Records",
            ANIME_RECORD_SUBTAB = "Anime",
            USER_RECORD_SUBTAB = "User",
            STAFF_RECORD_SUBTAB = "Staff",
            STUDIO_RECORD_SUBTAB = "Studio",

            TRANSACTIONS_TAB = "Transactions",
            WATCH_EPISODE_TRANSACTION_SUBTAB = "Watch Episode",
            RATE_ANIME_TRANSACTION_SUBTAB = "Rate Anime",
            EDIT_CREDITS_TRANSACTION_SUBTAB = "Edit Credits",
            FOLLOW_USER_TRANSACTION_SUBTAB = "Follow User",

            REPORTS_TAB = "Reports",
            HIGHEST_RATED_ANIME_REPORT_SUBTAB = "Highest Rated Anime",
            RECOMMEND_ANIME_REPORT_SUBTAB = "Recommend Anime",
            TOP_STUDIOS_REPORT_SUBTAB = "Top Studios",
            USER_PROFILE_REPORT_SUBTAB = "User Profile";

    // Stores the current tab and subtab name.
    private int currentTabIndex = 0;
    private int[] currentSubtabIndex = {
            0, 0, 0
    };

    /**
     * Initialize the top view.
     */
    public TopView() {
        frame = WidgetFactory.createJFrame("CCINFOM MCO GUI Prototype");
        this.frame.setLayout(new BorderLayout());
        this.frame.setBounds(100, 100, 1000, 500);

        this.instantiateWidgets();
        this.placeWidgets();
        this.frame.setVisible(true);
    }

    /**
     * Instantiate the widgets of the TopView.
     */
    public void instantiateWidgets() {
        tabs = WidgetFactory.createJTabbedPane();

        recordTables = new HashMap<>();
        mainTabs = new HashMap<>();
        tabMap = new LinkedHashMap<>();
        tabNames = new ArrayList<>();
        subtabNames = new ArrayList<>();

        for (Records record : Records.values()) {
            recordTables.put(record.name,
                    new RecordTable(frame, record.name, record.noun, record.shownColumnNames));
        }

        Collections.addAll(tabNames, RECORDS_TAB, TRANSACTIONS_TAB, REPORTS_TAB);

        Tab recordsTab = new Tab(RECORDS_TAB);
        ArrayList<Subtab> recordsTabSubtabs = new ArrayList<>();
        Collections.addAll(recordsTabSubtabs,
                new Subtab(ANIME_RECORD_SUBTAB, "records/anime.json"),
                new Subtab(USER_RECORD_SUBTAB, "records/user.json"),
                new Subtab(STAFF_RECORD_SUBTAB, "records/staff.json"),
                new Subtab(STUDIO_RECORD_SUBTAB, "records/studio.json"));
        tabMap.put(recordsTab, recordsTabSubtabs);
        ArrayList<String> recordsTabSubtabNames = new ArrayList<>();
        Collections.addAll(recordsTabSubtabNames, ANIME_RECORD_SUBTAB,
                USER_RECORD_SUBTAB, STAFF_RECORD_SUBTAB,
                STUDIO_RECORD_SUBTAB);
        subtabNames.add(recordsTabSubtabNames);

        Tab transactionsTab = new Tab(TRANSACTIONS_TAB);
        ArrayList<Subtab> transactionsTabSubtabs = new ArrayList<>();
        Collections.addAll(transactionsTabSubtabs,
                new Subtab(WATCH_EPISODE_TRANSACTION_SUBTAB, "transactions/watch_episode.json"),
                new Subtab(RATE_ANIME_TRANSACTION_SUBTAB, "transactions/rate_anime.json"),
                new Subtab(EDIT_CREDITS_TRANSACTION_SUBTAB, "transactions/edit_credits.json"),
                new Subtab(FOLLOW_USER_TRANSACTION_SUBTAB, "transactions/follow_user.json"));
        tabMap.put(transactionsTab, transactionsTabSubtabs);
        ArrayList<String> transactionsTabSubtabsNames = new ArrayList<>();
        Collections.addAll(transactionsTabSubtabsNames, WATCH_EPISODE_TRANSACTION_SUBTAB,
                RATE_ANIME_TRANSACTION_SUBTAB, EDIT_CREDITS_TRANSACTION_SUBTAB,
                FOLLOW_USER_TRANSACTION_SUBTAB);
        subtabNames.add(transactionsTabSubtabsNames);

        Tab reportsTab = new Tab(REPORTS_TAB);
        ArrayList<Subtab> reportsTabSubtabs = new ArrayList<>();
        Collections.addAll(reportsTabSubtabs,
                new Subtab(HIGHEST_RATED_ANIME_REPORT_SUBTAB, "reports/highest_rated_anime.json"),
                new Subtab(RECOMMEND_ANIME_REPORT_SUBTAB, "reports/recommend_anime.json"),
                new Subtab(TOP_STUDIOS_REPORT_SUBTAB, "reports/top_studios.json"),
                new Subtab(USER_PROFILE_REPORT_SUBTAB, "reports/user_profile.json"));
        tabMap.put(reportsTab, reportsTabSubtabs);
        ArrayList<String> reportsTabSubtabsNames = new ArrayList<>();
        Collections.addAll(reportsTabSubtabsNames, HIGHEST_RATED_ANIME_REPORT_SUBTAB,
                RECOMMEND_ANIME_REPORT_SUBTAB, TOP_STUDIOS_REPORT_SUBTAB,
                USER_PROFILE_REPORT_SUBTAB);
        subtabNames.add(reportsTabSubtabsNames);
    }

    /**
     * Place the widgets on the TopView.
     */
    public void placeWidgets() {
        for (HashMap.Entry<Tab, ArrayList<Subtab>> tabMapData : this.tabMap.entrySet()) {
            Tab tab = tabMapData.getKey();
            for (Subtab subtab : tabMapData.getValue()) {
                tab.addSubtab(subtab);
            }
            this.mainTabs.put(tab.getName(), tab);
            WidgetFactory.addTab(tabs, tab);
        }
        this.frame.add(tabs, BorderLayout.CENTER);
    }

    // Listeners

    /**
     * Set an action listener to the buttons of asubtab.
     * 
     * @param tabName     The name of the (primary) tab, e.g.,
     *                    {@link TopView#RECORDS_TAB}.
     * @param subtabName  The name of a subtab in the tab, e.g.,
     *                    {@link TopView#ANIME_RECORD_SUBTAB}.
     * @param listener    The action listener to set.
     * @param buttonNames The names of the buttons being set to; check the
     *                    associated subtab JSON file to inspect these.
     */
    public void setActionListeners(
            String tabName, String subtabName,
            ActionListener listener,
            String... buttonNames) {
        Subtab subtab = mainTabs.get(tabName).getSubtab(subtabName);
        System.out.println("Setting ActionListeners...");
        for (String name : buttonNames) {
            System.out.printf("\tComponent %s gets %s\n", name, listener.toString());
            subtab.setActionListener(name, listener);
        }
    }

    // Record Tables

    /**
     * This method is called once for each record table in order to initialize its
     * data. Unlike {@link #setRecordTableData()}, this also "adds" the JTable
     * itself into the RecordTable widget, hence the distinction.
     * 
     * @param recordName
     * @param data
     * @param column
     */
    public void initializeRecordTableData(String recordName, String[][] data, String[] column) {
        recordTables.get(recordName).initializeData(data, column);
    }

    /**
     * This method is called whenever a record table's data is updated and needs to
     * be set again.
     * 
     * @param recordName
     * @param data
     * @param column
     */
    public void setRecordTableData(String recordName, String[][] data, String[] column) {
        recordTables.get(recordName).setData(data, column);
    }

    public HashMap<String, String> getSelectedRowData(Records record) {
        return recordTables.get(record.name).getSelectedRowData();
    }

    public HashMap<String, String> getLastRowData(Records record) {
        return recordTables.get(record.name).getLastRowData();
    }

    /**
     * Set the record table listener for a certain record table.
     * 
     * @param recordName
     * @param listener
     */
    public void setRecordTableListener(String recordName, RecordTableListener listener) {
        recordTables.get(recordName).setListener(listener);
    }

    /**
     * "Pop-up" a record table, prompting the user to select from it.
     * 
     * @param recordName the name of the record (table) to display.
     */
    public void selectFromTable(Records record) {
        recordTables.get(record.name).setVisible(true);
    }

    /**
     * Get the selected row index of a record table.
     * 
     * @param recordName
     * @return the row index of the selected entry.
     */
    public int getSelected(String recordName) {
        return recordTables.get(recordName).getSelected();
    }

    /**
     * Set the visibility of a record table.
     * 
     * @param recordName The name of the record table
     * @param b          Boolean -- true for visible, false otherwise
     */
    public void setRecordTableVisible(String recordName, boolean b) {
        this.recordTables.get(recordName).setVisible(b);
    }

    // Subtab

    public void setTabListeners(CurrentTabListener currentTabListener, CurrentSubtabListener currentSubtabListener) {
        tabs.getModel().addChangeListener(currentTabListener);
        for (Tab tab : tabMap.keySet()) {
            tab.addChangeListener(currentSubtabListener);
        }
    }

    /**
     * Get a subtab.
     * 
     * @param mainTab The name of the tab containing the subtab.
     * @param subTab  The name of the subtab itself.
     * @return
     */
    public Subtab getSubtab(String mainTab, String subTab) {
        return mainTabs.get(mainTab).getSubtab(subTab);
    }

    public Subtab getCurrentSubtab() {
        return mainTabs.get(getCurrentTabName()).getSubtab(getCurrentSubtabName());
    }

    /**
     * Reset the fields of a subtab.
     * 
     * @param tabName    The name of the tab containing the subtab.
     * @param subtabName The name of the subtab itself.
     */
    public void resetFields(String tabName, String subtabName) {
        this.getSubtab(tabName, subtabName).resetFields();
    }

    /**
     * 
     * @param mainTab   The name of the tab containing the subtab.
     * @param subTab    The name of the subtab itself.
     * @param component The name of the component.
     * @return
     */
    public JComponent getComponent(String mainTab, String subTab, String component) {
        return this.getSubtab(mainTab, subTab).getComponent(component);
    }

    public String getCurrentTabName() {
        return tabNames.get(currentTabIndex);
    }

    public String getCurrentSubtabName() {
        return subtabNames.get(currentTabIndex).get(currentSubtabIndex[currentTabIndex]);
    }

    public void setCurrentTabName(int tabIndex) {
        this.currentTabIndex = tabIndex;
        System.out.println(getCurrentTabName());
    }

    public void setCurrentSubtabName(int subtabIndex) {
        this.currentSubtabIndex[currentTabIndex] = subtabIndex;
    }

    /**
     * Set the components of the current Subtab with data. Inspect the JSON files'
     * <code>db_link</code> field in order to identify and specify which components
     * get which data.
     * 
     * An entry like
     * 
     * <pre>
     *      {"name": "userId", "db_link": "users.user_id"}
     * </pre>
     * 
     * in the current Subtab would mean that if data included an entry
     * 
     * <pre>
     *      {"users.user_id": "12"}
     * </pre>
     * 
     * then the component named "userId" would have its text set with 12.
     * 
     * @param data A HashMap mapping column names of a record to their corresponding
     *             data values.
     */
    public void setFieldsFromData(HashMap<String, String> data) {
        System.out.printf("Setting fields in %s/%s\n", getCurrentTabName(), getCurrentSubtabName());
        Subtab subtab = mainTabs.get(getCurrentTabName()).getSubtab(getCurrentSubtabName());
        subtab.setFields(data);
    }

    // General pop-ups

    /**
     * Display a raw dialog with a table and a custom title.
     * 
     * @param data
     * @param columns
     * @param title
     */
    public void displayTable(String[][] data, String[] columns, String title) {
        JDialog tablePanes = WidgetFactory.createJDialog(frame, title);
        tablePanes.add(WidgetFactory.createJTableInScrollPane(data, columns), BorderLayout.CENTER);
        tablePanes.setVisible(true);
    }

    /**
     * Alert the user with a dialog pop-up.
     * 
     * @param title
     * @param message
     */
    public void dialogPopUp(String title, String message) {
        JDialog popup = WidgetFactory.createJDialog(frame, title, WidgetFactory.POPUP_SIZE);
        JLabel messageLabel = WidgetFactory.createJLabel(message, SwingConstants.CENTER);
        messageLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        popup.add(messageLabel, BorderLayout.CENTER);
        popup.setVisible(true);
    }

    /**
     * Alert the user of an error occurring.
     * 
     * @param title
     * @param message
     */
    public void errorPopUp(String title, String message) {
        JDialog popup = WidgetFactory.createJDialog(frame, title, WidgetFactory.POPUP_SIZE);
        JLabel messageLabel = WidgetFactory.createJLabel(message, SwingConstants.CENTER);
        messageLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        popup.add(messageLabel, BorderLayout.CENTER);
        popup.setVisible(true);
    }

    // Specialized pop-ups

    public void displayHighestRatedAnimes(HashMap<String[], String> data, String mode) {
        JDialog highestRatedPane = WidgetFactory.createJDialog(frame, "Highest Rated Anime " + mode);
        JTabbedPane tabbedPane = WidgetFactory.createJTabbedPane();

        HashSet<String> tabs = new HashSet<String>(data.values());
        for (String tab : tabs) {
            tabbedPane.addTab(tab, createTabTableHighestRated(tab, data));
        }
        highestRatedPane.add(tabbedPane, BorderLayout.CENTER);

        highestRatedPane.setVisible(true);
    }

    private JScrollPane createTabTableHighestRated(String tabName, HashMap<String[], String> data) {
        String[] columns = {
                "Rank", "Anime Title", "Genre", "Studio", "Average Rating"
        };
        ArrayList<String[]> tableValues = new ArrayList<String[]>();

        int rank = 1;
        for (Map.Entry<String[], String> entry : data.entrySet()) {
            String[] key = entry.getKey();
            String value = entry.getValue();
            if (tabName.equals(value)) {
                tableValues.add(key);
            }
        }

        String[][] tableValuesArr = tableValues.toArray(new String[0][0]);
        Arrays.sort(tableValuesArr, (a, b) -> {
            double ratingA = Double.parseDouble(a[4]);
            double ratingB = Double.parseDouble(b[4]);
            return Double.compare(ratingB, ratingA);
        });

        for (String[] strings : tableValuesArr) {
            strings[0] = Integer.toString(rank);
            strings[2] = Genre.findName(strings[2]);
            rank++;
        }
        return WidgetFactory.createJTableInScrollPane(tableValuesArr, columns);
    }

    public void displayRecommendations(String[][] data, String[] columns, String mode, String username) {
        JDialog recommendationPanes = WidgetFactory.createJDialog(frame, "Anime Recommendations");

        if (mode.equals("From Top Genres Watched")) {
            for (String[] row : data) {
                row[0] = Genre.findName(row[0]);
            }
        }

        JLabel topLabel = WidgetFactory.createJLabel(
                String.format("%s recommendations for %s", mode, username),
                Fonts.SUBTITLE);

        recommendationPanes.add(topLabel, BorderLayout.NORTH);
        recommendationPanes.add(WidgetFactory.createJTableInScrollPane(data, columns), BorderLayout.CENTER);

        recommendationPanes.setVisible(true);
    }

    public void displayTopStudios(String[][] data, String year) {
        JDialog topStudiosPane = WidgetFactory.createJDialog(frame, "Top Studios");

        JLabel topLabel = WidgetFactory.createJLabel(
                "Top Studios" + (year.equals("0") ? "" : (" For " + year)),
                Fonts.SUBTITLE);
        topStudiosPane.add(topLabel, BorderLayout.NORTH);
        topStudiosPane.add(WidgetFactory.createJTableInScrollPane(data,
                new String[] {
                        "Studio Name", "Studio Rating across all animes", "Top Rated Anime"
                }), BorderLayout.CENTER);

        topStudiosPane.setVisible(true);
    }

    public void displayUserProfile(String[] userDetails, String[][] userGenres) {
        JDialog profilePopup = WidgetFactory.createJDialog(frame, "User Profile");
        JTabbedPane profileTabs = WidgetFactory.createJTabbedPane();
        profileTabs.setTabPlacement(JTabbedPane.TOP);
        String[] formatSequence = {
                "userName", "region", "joinDate", "viewedAnimes", "totalEpisodes", "ratingsMade"
        };
        Subtab defaultProfile = new Subtab(userDetails[0] + " Profile", "reports/user_profile_template.json");
        for (int i = 0; i < userDetails.length; i++) {
            if (i == 1) {
                userDetails[i] = UserRegion.findName(userDetails[i]);
            }
            defaultProfile.setComponentText(formatSequence[i], userDetails[i]);
        }

        Subtab genreProfile = new Subtab(userDetails[0] + "'s Top 3 Genres", "reports/user_profile_genre.json");
        int i = 1;
        for (String[] row : userGenres) {
            genreProfile.setComponentText("rank" + i, Genre.findName(row[0]));
            genreProfile.setComponentText("topAnime" + i, row[2]);
            i++;
        }
        profileTabs.addTab(defaultProfile.getName(), defaultProfile);
        profileTabs.addTab(genreProfile.getName(), genreProfile);
        profilePopup.add(profileTabs);

        profilePopup.setVisible(true);
    }
}