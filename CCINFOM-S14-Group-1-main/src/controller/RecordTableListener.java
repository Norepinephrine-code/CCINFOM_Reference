package src.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;

import src.model.AnimeSystem;
import src.model.Records;
import src.view.gui.Subtab;
import src.view.gui.TopView;

/**
 * Listener for the RecordTable widget.
 */
public class RecordTableListener implements ActionListener {
    private Records associatedRecord;
    private AnimeSystem animeSystem;
    private TopView topView;

    public RecordTableListener(AnimeSystem animeSystem, TopView topView, Records associatedRecord) {
        this.animeSystem = animeSystem;
        this.topView = topView;
        this.associatedRecord = associatedRecord;
        this.setData();
    }

    public void setData() {
        String[] columns;
        String[][] data;

        try {
            if (associatedRecord != Records.ANIME) {
                columns = animeSystem.getRecordColNames(associatedRecord.name);
                data = animeSystem.selectColumns(columns, associatedRecord.name);
            } else {
                // Anime record table is special since we also want the studio names.
                columns = animeSystem.getRecordColNames(Records.ANIME.name, Records.STUDIO.name);
                data = animeSystem.selectColumns(columns,
                        "animes JOIN studios ON animes.studio_id = studios.studio_id");
            }
            topView.initializeRecordTableData(associatedRecord.name, data, columns);
        } catch (SQLException e) {
            topView.errorPopUp("RecordTable setData error", e.getMessage());
        }
    }

    /**
     * Override this method in order to customize what happens when a row is
     * selected.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        HashMap<String, String> rowData = topView.getSelectedRowData(associatedRecord);
        topView.setFieldsFromData(rowData);
        this.attemptRefreshLastWatched();
        topView.setRecordTableVisible(associatedRecord.name, false);
    }

    /**
     * Attempt to refresh any "last watched episode" labels, if any.
     */
    private void attemptRefreshLastWatched() {
        Subtab subtab = topView.getCurrentSubtab();
        try {
            String userId = subtab.getComponentText("userId");
            String animeId = subtab.getComponentText("animeId");
            try {
                Integer.parseInt(userId);
                Integer.parseInt(animeId);
            } catch (NumberFormatException e) {
                // Subtab does not have either userId or animeId.
                return;
            }
            subtab.setComponentText("episode",
                    animeSystem.getProcedureSingleResult(
                            String.format(
                                    "GetLastWatchedQ(%s, %s)",
                                    userId, animeId)));
        } catch (Exception e) {
            // Subtab does not have a last episode watched label.
            return;
        }

    }
}
