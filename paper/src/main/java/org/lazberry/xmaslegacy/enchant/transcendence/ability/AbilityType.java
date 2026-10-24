package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import org.bukkit.Material;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.logic.FlameWeaver;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.logic.SonicHunter;

@Getter
public enum AbilityType {
    SONIC_HUNTER(Material.ECHO_SHARD, SonicHunter.class),
    FLAME_WEAVER(Material.FIRE_CHARGE, FlameWeaver.class),;
    //AeroMancer(Material.WIND_CHARGE),
    //FrostMancer(Material.BLUE_ICE),
    //Dimensionist(Material.ENDER_PEARL);

    private final Material transcendenceMaterial;
	private final Class<? extends Ability> abilityClass;

    AbilityType(Material material, Class<? extends Ability> abilityClass) {
        this.transcendenceMaterial = material;
	    this.abilityClass = abilityClass;
    }
}
