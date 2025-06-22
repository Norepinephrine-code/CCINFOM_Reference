package src.model;

/**
 */
public enum Records {
    ANIME("animes", "an anime", "animes.title"),
    USER("users", "a user",
            "users.user_name"),
    STAFF("staff", "a staff member",
            "staff.first_name", "staff.last_name"),
    STUDIO("studios", "a studio",
            "studios.studio_name");

    public final String name;
    public final String noun;
    public final String[] shownColumnNames;

    private Records(String name, String noun, String... shownColumnNames) {
        this.name = name;
        this.noun = noun;
        this.shownColumnNames = shownColumnNames;
    }

    public static String getSortByColumn(String recordName) {
        String sortByCol;
        switch (recordName) {
            case "animes":
            case "animes JOIN studios ON animes.studio_id = studios.studio_id":
                /**
                 * The GUI implicitly joins the animes record with the studios record so it can
                 * grab the studio name.
                 */
                sortByCol = "animes.anime_id";
                break;
            case "users":
                sortByCol = "users.user_id";
                break;
            case "staff":
                sortByCol = "staff.staff_id";
                break;
            case "studios":
                sortByCol = "studios.studio_id";
                break;
            default:
                sortByCol = "id";
                break;
        }
        return sortByCol;
    }
}