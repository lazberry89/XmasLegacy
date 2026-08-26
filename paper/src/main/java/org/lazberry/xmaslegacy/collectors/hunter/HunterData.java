package org.lazberry.xmaslegacy.collectors.hunter;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@NoArgsConstructor
@Registry.Include(type = ServerType.MAIN)
public class HunterData {
    private final HunterBug hunterbug = new HunterBug();
    private final PhaseHunter phaseHunter = new PhaseHunter();
    private final Silence silence = new Silence();

    public HunterBug hunterBug() {return hunterbug;}
    public PhaseHunter phasehunter() {return phaseHunter;}
    public Silence silence() {return silence;}

    @Data
    public static class HunterBug {
        private EntityType hunterType1 = EntityType.SILVERFISH;
        private double hunter1chance = 0.5;
        private EntityType hunterType2 = EntityType.ENDERMITE;
        private double hunter2chance = 0.5;
        private double debuffChance = 0.4;
    }

    @Data
    public static class PhaseHunter {

    }

    @Data
    public static class Silence {

    }
}
