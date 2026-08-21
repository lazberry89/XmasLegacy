package org.lazberry.xmaslegacy.role.general;

import lombok.Data;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Include(type = ServerType.WILD)
public class RoleManager {
    private final Farmer farmer = new Farmer();
    private final Miner miner = new Miner();

    public Farmer farmer() { return farmer; }
    public Miner miner() { return miner; }

    @Data
    public static class Farmer {
        private int additionalDropsMin = 1;
        private int additionalDropsMax = 3;
        private int additionalExpMin = 3;
        private int additionalExpMax = 5;
        private int plantingExp = 1;
        private double plantingExpChance = 0.3;
        private double bonusGrowthChance = 0.25;
        private double instantMaxGrowthChance = 0.03;
        private int bonusGrowthMin = 1;
        private int bonusGrowthMax = 2;
    }

    @Data
    public static class Miner {
        private int expLowMin = 2;
        private int expLowMax = 5;
        private int expMediumMin = 5;
        private int expMediumMax = 10;
        private int expHighMin = 10;
        private int expHighMax = 18;
        private int expHighestMin = 20;
        private int expHighestMax = 35;
        private int expSpecialMin = 40;
        private int expSpecialMax = 60;
        private double searchChance = 0.4;
        private int searchRadius = 2;
        private int glowDuration = 2;
    }
}