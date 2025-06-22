package src.controller;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;

import src.model.AnimeSystem;
import src.view.gui.Subtab;
import src.view.gui.TopView;

/**
 * ReportsTabListener handles events in the Reports tab.
 */
public class ReportsTabListener extends TabListener {

    public ReportsTabListener(AnimeSystem animeSystem, TopView topView) {
        super(animeSystem, topView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = ((JComponent) e.getSource()).getName();
        System.out.println("\nTransactions/?buttonName=" + name);
        Subtab subtab;
        String period, genre, username, year;
        int user_id;

        switch (name) {
            case "checkButton":
                subtab = topView.getSubtab(TopView.REPORTS_TAB, TopView.HIGHEST_RATED_ANIME_REPORT_SUBTAB);
                period = subtab.getComponentText("periodComboBox", "period");
                genre = subtab.getComponentText("genreComboBox", "genre_with_none");
                HashMap<String[], String> data = generateHighestRatedAnime(period, genre);
                topView.displayHighestRatedAnimes(data, period);
                break;

            case "checkRecommendedAnime":
                subtab = topView.getSubtab(TopView.REPORTS_TAB, TopView.RECOMMEND_ANIME_REPORT_SUBTAB);
                try {
                    username = subtab.getComponentText("username");
                    year = subtab.getComponentText("yearRecommComboBox", "years");
                    user_id = Integer.parseInt(subtab.getComponentText("userId"));
                    System.out.println(user_id + username);
                    String mode = subtab.getComponentText("recommendationComboBox", "recommendations");
                    this.generateRecommendations(mode, Integer.parseInt(year), user_id, username);
                } catch (Exception ex) {
                    topView.dialogPopUp("Recommend Anime", "Error User field cannot be empty");
                }
                break;
            case "checkTopStudio":
                subtab = topView.getSubtab(TopView.REPORTS_TAB, TopView.TOP_STUDIOS_REPORT_SUBTAB);
                year = subtab.getComponentText("yearStudioComboBox", "years");
                System.out.println(year);
                this.generateTopStudios(Integer.parseInt(year));
                break;
            case "checkUserProfile":
                subtab = topView.getSubtab(TopView.REPORTS_TAB, TopView.USER_PROFILE_REPORT_SUBTAB);
                String userId = subtab.getComponentText("userId");
                if (!validateId(userId, "User profile", "Error: user field cannot be empty.")) {
                    return;
                }
                year = subtab.getComponentText("yearUserComboBox", "years");
                this.generateUserProfile(Integer.parseInt(userId), Integer.parseInt(year));
                break;
            case "searchUser":
                this.searchUser();
                break;
            default:
                System.err.println("No associated action for " + name);
                break;
        }

    }

    //

    public String[] removeFirstElement(String[] array) {
        if (array == null || array.length == 0) {
            return new String[0];
        }

        String[] newArray = new String[array.length - 1];
        System.arraycopy(array, 1, newArray, 0, array.length - 1);
        return newArray;
    }

    public HashMap<String[], String> generateHighestRatedAnime(String period, String genre) {
        try {
            animeSystem.callProcedure("SelectBestAnime" + period + "(?)", genre);
            String[][] results = animeSystem.rawQuery("SELECT * FROM `best_anime`");

            return stringArrayToMap(results);
        } catch (Exception e) {
            topView.dialogPopUp("Highest Rated Anime", "Error cant make report.");
            return null;
        }
    }

    public void generateRecommendations(String mode, int year, int user_id, String username) {
        String[][] data;

        try {
            switch (mode) {
                case "Continue Watching":
                    data = animeSystem.getProcedureResults("RecommendFromWatchList(?, ?)", user_id, year);
                    topView.displayRecommendations(data, new String[] {
                            "Anime Title", "Watched Episodes", "Total Episodes", "Date Released"
                    }, mode, username);
                    break;
                case "From Following":
                    data = animeSystem.getProcedureResults("RecommendFromFollows(?, ?)", user_id, year);
                    topView.displayRecommendations(data, new String[] {
                            "Followed User", "Anime Title", "Comments", "Rating", "Date Released"
                    }, mode, username);
                    break;
                case "From Top Genres Watched":
                    data = animeSystem.getProcedureResults("RecommendFromGenre(?, ?)", user_id, year);
                    topView.displayRecommendations(data, new String[] {
                            "Genre", "Total episodes watched of Genre", "Top Unwatched Anime", "Date Released"
                    }, mode, username);
                    break;
            }
        } catch (Exception e) {
            topView.dialogPopUp("Recommend Anime", "Error cannot fetch recommendations");
        }
    }

    public void generateTopStudios(int year) {
        try {
            String[][] data = animeSystem.getProcedureResults("ViewBestStudio(?)", year);
            topView.displayTopStudios(data, Integer.toString(year));
        } catch (Exception e) {
            topView.dialogPopUp("Top Studios", "Error cannot fetch studio data");
        }
    }

    public void generateUserProfile(int user_id, int year) {
        String[][] data;
        String[] userDetails;
        try {
            data = animeSystem.getProcedureResults("ViewUserProfile(?, ?)", user_id, year);
            userDetails = data[0];

            data = animeSystem.getProcedureResults("ViewUserGenreAnime(?, ?)", user_id, year);

            topView.displayUserProfile(userDetails, data);
        } catch (Exception e) {
            topView.dialogPopUp("Top Studios", "Error cannot fetch recommendations");

        }
    }

    private HashMap<String[], String> stringArrayToMap(String[][] data) {
        int rows = data.length;
        HashMap<String[], String> resultMap = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            String value = data[i][0];
            String[] key = data[i];
            resultMap.put(key, value);
        }
        return resultMap;
    }
}
