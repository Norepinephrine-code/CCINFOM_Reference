package src.model;

public enum StaffDepartment {
    DP("DP", "Direction and Production"),
    AD("AD", "Arts and Design"),
    AN("AN", "Animation"),
    EV("EV", "Editing and Visual Effects"),
    SS("SS", "Script and Storyboarding"),
    TO("TO", "Technical and Other Staff");

    public final String code;
    public final String name;

    private StaffDepartment(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String findName(String code) {
        for (StaffDepartment StaffDepartments : StaffDepartment.values()) {
            if (StaffDepartments.code.equals(code)) {
                return StaffDepartments.name;
            }
        }
        return null;
    }

    public static String findCode(String name) {
        for (StaffDepartment StaffDepartments : StaffDepartment.values()) {
            if (StaffDepartments.name.equals(name)) {
                return StaffDepartments.code;
            }
        }
        return null;
    }
}