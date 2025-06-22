package src.controller;

import java.awt.event.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.HashMap;

import javax.swing.JComponent;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import src.model.AnimeSystem;
import src.model.Genre;
import src.model.Records;
import src.model.StaffDepartment;
import src.view.gui.Subtab;
import src.view.gui.TopView;

/**
 * RecordsTabListener handles events in the Records/Anime subtab.
 */
public class RecordsTabListener extends TabListener {

    public RecordsTabListener(AnimeSystem animeSystem, TopView topView) {
        super(animeSystem, topView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = ((JComponent) e.getSource()).getName();
        System.out.printf("%s/%s?%s\n", topView.getCurrentTabName(), topView.getCurrentSubtabName(), name);

        switch (name) {
            // Anime subtab
            case "searchAnime":
                searchAnime();
                break;
            case "addNewAnime":
                addNewAnime();
                break;
            case "saveAnime":
                saveAnime();
                break;
            case "deleteAnime":
                deleteAnime();
                break;

            // User subtab
            case "searchUser":
                searchUser();
                break;
            case "addNewUser":
                addNewUser();
                break;
            case "saveUser":
                saveUser();
                break;
            case "deleteUser":
                deleteUser();
                break;
            case "viewUserWatchHistory":
                viewUserWatchHistory();
                break;

            // Staff subtab
            case "searchStaff":
                searchStaff();
                break;
            case "addNewStaff":
                addNewStaff();
                break;
            case "saveStaff":
                saveStaff();
                break;
            case "deleteStaff":
                deleteStaff();
                break;
            case "staffHistory":
                checkStaffHistory();
                break;

            // Studio subtab
            case "searchStudio":
                searchStudio();
                break;
            case "addNewStudio":
                addNewStudio();
                break;

            case "saveStudio":
                saveStudio();
                break;

            case "deleteStudio":
                deleteStudio();
                break;
            case "searchAllAnime":
                searchAllAnime();
                break;
            default:
                System.err.println("No action associated for " + name);
                break;
        }
    }

    // General

    public void refreshRecordTableData(Records record) {
        String recordNameColSelect = record.name;
        String recordName = record.name;
        String[] columns;
        String[][] data;

        if (record.name == Records.ANIME.name) {
            recordName = "animes JOIN studios ON animes.studio_id = studios.studio_id";
            columns = animeSystem.getRecordColNames(Records.ANIME.name, Records.STUDIO.name);
            // ^ Anime record is special since in the GUI we also need studio names.
            // ! Next time, use studio name as a primary key instead of a surrogate key, to
            // ! avoid these workarounds
        } else {
            columns = animeSystem.getRecordColNames(recordNameColSelect);
        }

        try {
            data = animeSystem.selectColumns(columns, recordName);
            topView.setRecordTableData(record.name, data, columns);
        } catch (SQLException e) {
            topView.errorPopUp(
                    "Record column retrieval error",
                    "Could not determine record columns for record <pre>"
                            + recordName
                            + "</pre>");
        }
    }

    public void setTopViewWithNewest(Records record) {
        this.refreshRecordTableData(record);
        HashMap<String, String> rowData = topView.getLastRowData(record);
        topView.setFieldsFromData(rowData);
    }

    // Anime records management

    public void searchAnime() {
        super.searchAnime();

        topView.getComponent(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB, "deleteAnime").setEnabled(true);
    }

    public void addNewAnime() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB);
        topView.resetFields(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB);

        subtab.setComponentText(topView.getComponent(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB, "airDate"),
                String.valueOf(LocalDate.now()));

        topView.getComponent(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB, "deleteAnime").setEnabled(false);
    }

    /**
     * Attempt to save an anime. Validation is performed to the best of its ability
     * before actually sending it to the database. Afterwards, either inserts or
     * updates based on the validity of animeId.
     */
    public void saveAnime() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB);
        String animeId = subtab.getComponentText("animeId");
        String studioId = subtab.getComponentText("studioId");
        String animeTitle = subtab.getComponentText("animeTitle");
        String genre = subtab.getComponentText("genre", "genre");
        String airDate = subtab.getComponentText("airDate");
        String episodes = subtab.getComponentText("episodes");

        if (!validateId(studioId, "No studio selected", "Please select a studio")) {
            return;
        }

        if (!validateDate(airDate)) {
            return;
        }

        if (!validateNotEmpty(animeTitle, "anime title")) {
            return;
        }

        if (!validateLength(animeTitle, "anime title", 64)) {
            return;
        }

        try {
            Integer.parseInt(animeId);
            // Valid animeId, so we're updating an existing anime
            updateAnime(animeId, studioId, animeTitle, genre, airDate, episodes);
        } catch (NumberFormatException exception) {
            // Invalid animeId, so we're creating a new one
            createAnime(studioId, animeTitle, genre, episodes);
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB, "deleteAnime").setEnabled(true);
    }

    public void createAnime(String studioId, String animeTitle, String genre, String episodes) {
        try {
            String query = """
                    INSERT INTO `animes` (`studio_id`, `title`, `genre`, `air_date`, `num_of_episodes`)
                    VALUES (?, ?, ?, NOW(), ?)
                    """;
            animeSystem.safeUpdate(query, studioId, animeTitle, genre, episodes);
            this.topView.dialogPopUp("Successfully inserted new anime",
                    String.format("Anime %s has been successfully inserted into the database.", animeTitle));
            this.setTopViewWithNewest(Records.ANIME);
        } catch (MysqlDataTruncation exception) {
            topView.errorPopUp("Invalid date error", "Invalid date.");
        } catch (SQLException exception) {
            topView.errorPopUp("Invalid episode count", "Invalid number of episodes.");
        }
    }

    public void updateAnime(String animeId, String studioId, String animeTitle, String genre, String airDate,
            String episodes) {
        String checkCurrentEpisodeCount = """
                SELECT num_of_episodes
                FROM animes
                WHERE anime_id = ?
                """;
        int currentEpisodeCount = 1;

        try {
            HashMap<String, String> data = animeSystem.safeSingleQuery(checkCurrentEpisodeCount, animeId);
            currentEpisodeCount = Integer.parseInt(data.get("num_of_episodes"));
        } catch (SQLIntegrityConstraintViolationException exception) {
            topView.errorPopUp("Anime", "Anime title must be unique");
        } catch (Exception e) {
            System.out.println("error occurred: " + e);
        }

        try {
            String query = """
                    UPDATE  `animes`
                    SET     `studio_id` = ?,
                    `title` = ?,
                    `genre` = ?,
                    `air_date` = ?,
                    `num_of_episodes` = ?
                    WHERE `anime_id` = ?
                    """;
            if (currentEpisodeCount > Integer.parseInt(episodes))
                throw new SQLException();
            animeSystem.safeUpdate(query, studioId, animeTitle, genre, airDate, episodes, animeId);

            this.topView.dialogPopUp("Successfully updated anime",
                    String.format(
                            "Anime %s has been successfully updated in the database.",
                            animeTitle));

            this.refreshRecordTableData(Records.ANIME);
        } catch (MysqlDataTruncation exception) {
            topView.errorPopUp("Anime", (animeTitle.length() > 64) ? "Title is too long" : "Invalid Date");
        } catch (SQLException exception) {
            topView.errorPopUp("Anime", "Invalid Number of Episodes");
        }
    }

    public void deleteAnime() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB);
        String animeId = subtab.getComponentText("animeId");

        try {
            animeSystem.safeUpdate("DELETE FROM `animes` WHERE `anime_id` = ?", animeId);
            this.refreshRecordTableData(Records.ANIME);

        } catch (SQLIntegrityConstraintViolationException Exception) {
            topView.errorPopUp("Anime", "Could not delete due to existing transactions connected to "
                    + subtab.getComponentText("animeTitle"));
        } catch (SQLException exception) {
            topView.errorPopUp("SQLException", exception.getMessage());
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.ANIME_RECORD_SUBTAB, "deleteAnime").setEnabled(false);
    }

    // User records management

    public void searchUser() {
        super.searchUser();
        topView.getComponent(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB, "deleteUser").setEnabled(true);
    }

    /**
     * Reset the fields of the user record subtab -- this "prompts" the user to
     * input data for a new user.
     */
    public void addNewUser() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB);
        topView.resetFields(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB);

        subtab.setComponentText(topView.getComponent(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB, "joinDate"),
                String.valueOf(LocalDate.now()));

        topView.getComponent(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB, "deleteUser").setEnabled(false);
    }

    /**
     * Save user button either updates an existing user or creates a new user.
     */
    public void saveUser() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB);
        String userId = subtab.getComponentText("userId");
        String username = subtab.getComponentText("username");
        String region = subtab.getComponentText("region", "region");
        String joinDate = subtab.getComponentText("joinDate");
        try {
            Integer.parseInt(userId);
            // User ID field was parsed successfully; this must be an existing record
            updateUser(userId, username, region, joinDate);
        } catch (NumberFormatException exception) {
            // This must be a new record
            createUser(username, region, joinDate);
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB, "deleteUser").setEnabled(true);
    }

    public void createUser(String username, String region, String joinDate) {
        if (username.equals("")) {
            topView.errorPopUp("User", "Username cannot be empty");
            return;
        }
        if (!validateDate(joinDate)) {
            return;
        }

        try {
            animeSystem.safeUpdate(
                    "INSERT INTO `users` (`user_name`, `region`, `join_date`) VALUES (?, ?, ?)",
                    username, region, joinDate);
            this.setTopViewWithNewest(Records.USER);
            topView.dialogPopUp("User add success", "Successfully added user " + username);
        } catch (SQLIntegrityConstraintViolationException exception) {
            topView.errorPopUp("User", "Username must be unique");
        } catch (SQLException exception) {
            System.out.println("Exception class = " + exception.getClass());
            topView.errorPopUp("SQLException", exception.getMessage());
        }
    }

    public void updateUser(String userId, String username, String region, String joinDate) {
        try {
            animeSystem.safeUpdate(
                    "UPDATE `users` SET `user_name` = ?, `region` = ?, `join_date` = ? WHERE `user_id` = ?",
                    username, region, joinDate, userId);
            this.refreshRecordTableData(Records.USER);
            topView.dialogPopUp("User updated success", "Successfully updated user " + username);
        } catch (SQLException exception) {
            System.out.println("Exception class = " + exception.getClass());
            topView.errorPopUp("SQLException", exception.getMessage());
        }
    }

    public void deleteUser() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB);
        String userId = subtab.getComponentText("userId");
        if (!validateId(
                userId,
                "User ID blank",
                "Please select a user to delete.")) {
            return;
        }
        try {
            animeSystem.safeUpdate("DELETE FROM `users` WHERE `user_id` = ?", userId);
            this.refreshRecordTableData(Records.USER);
        } catch (SQLIntegrityConstraintViolationException Exception) {
            topView.errorPopUp("User", "Could not delete due to existing transactions connected to "
                    + subtab.getComponentText("username"));
        } catch (SQLException exception) {
            topView.errorPopUp("SQLException", exception.getMessage());
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.USER_RECORD_SUBTAB, "deleteUser").setEnabled(false);
    }

    private void viewUserWatchHistory() {
        String userId = topView.getCurrentSubtab().getComponentText("userId");
        if (!validateId(
                userId,
                "No user selected",
                "Please select a user to view the watch history of.")) {
            return;
        }

        String userName = topView.getCurrentSubtab().getComponentText("username");
        String query = String.format("""
                SELECT views.timestamp_watched, animes.title, views.watched_episode
                FROM animes
                JOIN views ON animes.anime_id  = views.anime_id
                AND views.user_id = %s
                ORDER BY timestamp_watched DESC, animes.title, views.watched_episode
                """, userId);
        try {
            String[][] data = animeSystem.rawQuery(query);
            for (String[] row : data) {
                for (String s : row) {
                    System.out.print(s + '\t');
                }
                System.out.println();
            }
            topView.displayTable(data, new String[] {
                    "Timestamp", "Anime", "Episode"
            }, "Watch History of " + userName);
        } catch (SQLException e) {
            e.printStackTrace();
            topView.errorPopUp("Unhandled SQLException", e.getMessage());
        }

    }

    // Staff records management

    public void searchStaff() {
        super.searchStaff();
        topView.getComponent(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB, "deleteStaff").setEnabled(true);
    }

    public void addNewStaff() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB);
        topView.resetFields(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB);

        subtab.setComponentText(topView.getComponent(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB, "birthday"),
                "1970-01-01");

        topView.getComponent(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB, "deleteStaff").setEnabled(false);
    }

    public void saveStaff() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB);
        String staffId = subtab.getComponentText("staffId");
        String firstName = subtab.getComponentText("firstName");
        String lastName = subtab.getComponentText("lastName");
        String occupation = subtab.getComponentText("occupation");
        String birthday = subtab.getComponentText("birthday");

        try {
            Integer.parseInt(staffId);
            updateStaff(staffId, firstName, lastName, occupation, birthday);
        } catch (NumberFormatException exception) {
            createStaff(firstName, lastName, occupation, birthday);
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB, "deleteStaff").setEnabled(true);
    }

    public void createStaff(String firstName, String lastName, String occupation, String birthday) {
        if (firstName.equals("") || lastName.equals("")) {
            topView.errorPopUp("Staff", "First and last name cannot be empty");
            return;
        }

        try {
            animeSystem.safeUpdate(
                    "INSERT INTO `staff` (`first_name`, `last_name`, `occupation`, `birthday`) VALUES (?, ?, ?, ?)",
                    firstName, lastName, occupation, birthday);
            this.setTopViewWithNewest(Records.STAFF);
            topView.dialogPopUp("Staff", "Successfully created staff entry!");
        } catch (MysqlDataTruncation exception) {
            topView.errorPopUp("Staff",
                    (firstName.length() > 16) ? "First Name is too long"
                            : (lastName.length() > 16) ? "Last Name is too long"
                                    : (occupation.length() > 32) ? "Occupation name is too long" : "Invalid Date");
        } catch (SQLException exception) {
            System.out.println("Exception class = " + exception.getClass());
            topView.errorPopUp("SQLException", exception.getMessage());
        }
    }

    public void updateStaff(String staffId, String firstName, String lastName, String occupation, String birthday) {
        try {
            animeSystem.safeUpdate(
                    "Update `staff` SET `first_name` = ?, `last_name` = ?, `occupation` = ?, `birthday` = ? WHERE `staff_id` = ?",
                    firstName, lastName, occupation, birthday, staffId);
            this.refreshRecordTableData(Records.STAFF);
            topView.dialogPopUp("Staff", "Successfully updated staff entry!");
        } catch (MysqlDataTruncation exception) {
            topView.errorPopUp("Staff",
                    (firstName.length() > 16) ? "First Name is too long"
                            : (lastName.length() > 16) ? "Last Name is too long"
                                    : (occupation.length() > 32) ? "Occupation name is too long" : "Invalid Date");
        } catch (SQLException exception) {
            System.out.println("Unhandled SQLException class " + exception.getClass());
            topView.errorPopUp("Unhandled SQLException", exception.getMessage());
        }
    }

    public void deleteStaff() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB);
        String staffId = subtab.getComponentText("staffId");

        try {
            animeSystem.safeUpdate("DELETE FROM `staff` WHERE `staff_id` = ?", staffId);
            this.refreshRecordTableData(Records.STAFF);
        } catch (SQLIntegrityConstraintViolationException Exception) {
            topView.errorPopUp("Staff", "Could not delete due to existing transactions connected to "
                    + subtab.getComponentText("firstName") + " " + subtab.getComponentText("lastName"));
        } catch (SQLException exception) {
            topView.errorPopUp("SQLException", exception.getMessage());
        }

        topView.getComponent(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB, "deleteStaff").setEnabled(false);
    }

    public void checkStaffHistory() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STAFF_RECORD_SUBTAB);
        String[][] data;
        String staffId = subtab.getComponentText("staffId");
        String firstName = subtab.getComponentText("firstName");
        String lastName = subtab.getComponentText("lastName");

        String query = String.format("""
                    SELECT CONCAT(s.first_name, " ", s.last_name) AS staff_name,
                a.title, c.episode, c.position, c.department
                FROM credits c
                JOIN staff s ON s.staff_id = c.staff_id
                JOIN animes a ON c.anime_id = a.anime_id
                WHERE s.staff_id = %s
                ORDER BY a.anime_id
                    """, staffId);

        try {
            Integer.parseInt(staffId);
            data = animeSystem.rawQuery(query);
            // 4th column is department enum -- replace with full name
            for (int i = 0; i < data.length; i++) {
                data[i][4] = StaffDepartment.findName(data[i][4]);
            }
            topView.displayTable(data,
                    new String[] {
                            "Staff Name", "Anime Title", "Episode", "Position", "Department"
                    },
                    new String(firstName + " " + lastName + "'s Work History"));
        } catch (Exception exception) {
            topView.errorPopUp("Staff", "Cannot fetch staff history");
            System.out.println(exception.getMessage());
        }
    }

    // Studio records management
    public void addNewStudio() {
        topView.resetFields(TopView.RECORDS_TAB, TopView.STUDIO_RECORD_SUBTAB);
    }

    public void saveStudio() {

        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STUDIO_RECORD_SUBTAB);
        String studioId = subtab.getComponentText("studioId");
        String studio_name = subtab.getComponentText("studioName");

        if (validateNotEmpty(studio_name, "studio name") == false) {
            return;
        }

        try {
            Integer.parseInt(studioId);
            // User ID field was parsed successfully; this must be an existing record
            updateStudio(studioId, studio_name);
            topView.dialogPopUp("Studio", "Studio name successfully changed to " + studio_name + ".");
        } catch (NumberFormatException exception) {
            createStudio(studio_name);
            topView.dialogPopUp("Studio", "Studio " + studio_name + " successfully created.");
        }
    }

    public void createStudio(String studio_name) {
        try {
            animeSystem.safeUpdate(
                    "INSERT INTO `studios` (`studio_name`) VALUES (?)",
                    studio_name);
            this.setTopViewWithNewest(Records.STUDIO);
        } catch (SQLException exception) {
            topView.dialogPopUp("SQLException", exception.getMessage());
        }
    }

    public void updateStudio(String studioID, String studio_name) {
        try {
            animeSystem.safeUpdate(
                    "UPDATE `studios` SET `studio_name` = ? WHERE `studio_id` = ?",
                    studio_name, studioID);
            this.refreshRecordTableData(Records.STUDIO);
        } catch (SQLException exception) {
            topView.dialogPopUp("SQLException", exception.getMessage());
        }
    }

    public void deleteStudio() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STUDIO_RECORD_SUBTAB);
        String studio_id = subtab.getComponentText("studioId");
        String studio_name = subtab.getComponentText("studioName");

        if (!validateId(studio_id, "No studio selected", "Please select a studio to delete")) {
            return;
        }
        try {
            animeSystem.safeUpdate("DELETE FROM `studios` WHERE `studio_id` = ?", studio_id);
            this.refreshRecordTableData(Records.STUDIO);
            topView.dialogPopUp("Studio", "Deletion of Studio " + studio_name + " successful.");
            topView.resetFields(TopView.RECORDS_TAB, TopView.STUDIO_RECORD_SUBTAB);
        } catch (SQLIntegrityConstraintViolationException Exception) {
            topView.errorPopUp("Studio", "Could not delete due to existing animes.");
        } catch (SQLException exception) {
            topView.errorPopUp("SQLException", exception.getMessage());
        }
    }

    public void searchAllAnime() {
        Subtab subtab = topView.getSubtab(TopView.RECORDS_TAB, TopView.STUDIO_RECORD_SUBTAB);
        String studio_id = subtab.getComponentText("studioId");
        String studio_name = subtab.getComponentText("studioName");

        if (!validateId(
                studio_id,
                "No studio selected",
                "Please select a studio.")) {
            return;
        }

        try

        {
            String[][] data = animeSystem.rawQuery(
                    "SELECT title, genre, air_date, num_of_episodes FROM animes WHERE studio_id = " + studio_id);
            for (String[] strings : data) {
                strings[1] = Genre.findName(strings[1]);
            }
            topView.displayTable(data, new String[] {
                    "Anime Title", "Genre", "Air Date", "Episode Count"
            }, studio_name);
        } catch (SQLException e) {
            topView.errorPopUp("Unhandled SQLException", e.getMessage());
            e.printStackTrace();
        }

    }
}
