package src.model;

/**
 * ("Region" is already taken)
 */
public enum UserRegion {
    JP("JP", "Japan"),
    AM("AM", "Americas"),
    EU("EU", "Europe"),
    AS("AS", "Asia"),
    AU("AU", "Australia"),
    AF("AF", "Africa"),
    AC("AC", "Antarctica");

    public final String code;
    public final String name;

    private UserRegion(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String findName(String code) {
        for (UserRegion region : UserRegion.values()) {
            if (region.code.equals(code)) {
                return region.name;
            }
        }
        return null;
    }

    public static String findCode(String name) {
        for (UserRegion region : UserRegion.values()) {
            if (region.name.equals(name)) {
                return region.code;
            }
        }
        return null;
    }
}