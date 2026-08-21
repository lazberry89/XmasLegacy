package org.lazberry.xmaslegacy.role.general;

import lombok.Data;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Include(type = ServerType.WILD)
public class RoleManager {
    private final Farmer farmer = new Farmer();

    public Farmer farmer() {return farmer;}

    @Data
    public static class Farmer {
        private int additionalDropsMin = 1;
        private int additionalDropsMax = 3;
        private int additionalExpMin = 10;
        private int additionalExpMax = 30;
    }
}
