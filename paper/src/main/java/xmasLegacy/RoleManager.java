package xmasLegacy;

public class RoleManager {
    private final XmasLegacy plugin;
    private static RoleManager instance;

    public static RoleManager getInstance() {
        if (instance == null) instance = new RoleManager();
        return instance;
    }

    /*private RoleManager() {
        this.plugin = XmasLegacy.getInstance();*/
    }
}
