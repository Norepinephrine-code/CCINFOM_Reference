package src.model;

public enum Genre {
    AD("AD", "Adventure"),
    CO("CO", "Comedy"),
    DR("DR", "Drama"),
    HO("HO", "Horror"),
    SF("SF", "Sci-Fi");

    public final String code;
    public final String name;

    private Genre(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String findName(String code) {
        for (Genre genre : Genre.values()) {
            if (genre.code.equals(code)) {
                return genre.name;
            }
        }
        return null;
    }

    public static String findCode(String name) {
        for (Genre genre : Genre.values()) {
            if (genre.name.equals(name)) {
                return genre.code;
            }
        }
        return null;
    }
}