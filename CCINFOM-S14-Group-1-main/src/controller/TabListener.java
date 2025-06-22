package src.controller;

import java.awt.event.ActionListener;

import src.model.AnimeSystem;
import src.model.Records;
import src.view.gui.TopView;

/**
 * This abstract class holds some common functionality between the
 * RecordsTabListener, TransactionsTabListener, and ReportsTabListener classes.
 */
public abstract class TabListener implements ActionListener {
    AnimeSystem animeSystem;
    TopView topView;
    public static final String INVALID_DATE_STRING = "Please input a valid date in the YYYY-MM-DD format.";

    /**
     * By default, a TabListener takes in the {@link AnimeSystem} model and the
     * {@link TopView} view.
     * 
     * @param animeSystem
     * @param topView
     */
    public TabListener(AnimeSystem animeSystem, TopView topView) {
        this.animeSystem = animeSystem;
        this.topView = topView;
    }

    /**
     * Convenience method for searching an anime.
     */
    public void searchAnime() {
        topView.selectFromTable(Records.ANIME);
    }

    /**
     * Convenience method for searching a user.
     */
    public void searchUser() {
        topView.selectFromTable(Records.USER);
    }

    /**
     * Convenience method for searching a staff member.
     */
    public void searchStaff() {
        topView.selectFromTable(Records.STAFF);
    }

    /**
     * Convenience method for searching a studio.
     */
    public void searchStudio() {
        topView.selectFromTable(Records.STUDIO);
    }

    /**
     * Validate an id read as a String.
     * 
     * @param id
     * @param errorTitle
     * @param errorBody
     * @return true if `id` is valid, false otherwise
     */
    public boolean validateId(String id, String errorTitle, String errorBody) {
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            topView.errorPopUp(errorTitle, errorBody);
            return false;
        }
        return true;
    }

    /**
     * Validate that a text is not empty.
     * 
     * @param text   text to verify
     * @param entity the "entity name" used to build the error pop-up
     * @return true if text is not empty, false otherwise
     */
    public boolean validateNotEmpty(String text, String entity) {
        if (text.isEmpty()) {
            topView.errorPopUp(
                    "Empty " + entity,
                    "Error: " + entity + " must not be empty.");
            return false;
        }
        return true;
    }

    /**
     * Validate that a text is not longer than a certain limit.
     * 
     * @param text   text to verify
     * @param entity the "entity name" used to build the error pop-up
     * @return true if text is not empty, false otherwise
     */
    public boolean validateLength(String text, String entity, int limit) {
        if (text.length() > limit) {
            topView.errorPopUp(
                    "Too long: " + entity,
                    "Error: " + entity + " too long. Please limit to " + limit + "characters.");
            return false;
        }
        return true;
    }

    /**
     * Check if a String is a valid date in the YYYY-MM-DD format.
     * 
     * @param date
     * @return true if a valid date string is passed; false otherwise
     */
    public boolean validateDate(String date) {
        String[] tokens = date.split("-");
        for (String token : tokens) {
            try {
                Integer.parseInt(token);
            } catch (NumberFormatException e) {
                topView.errorPopUp("Date error", INVALID_DATE_STRING);
                return false;
            }
        }
        return true;
    }

}
