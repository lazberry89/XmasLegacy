package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum AbilityType {
    SonicHunter(Material.ECHO_SHARD, SonicHunter.class),
    FlameWeaver(Material.FIRE_CHARGE),
    AeroMancer(Material.WIND_CHARGE),
    FrostMancer(Material.BLUE_ICE),
    Dimensionist(Material.ENDER_PEARL);

    private final Material transcendenceMaterial;
    private final Class<? extends Ability> abilityClass;

    AbilityType(Material material, Class<? extends Ability> abilityClass) {
        this.transcendenceMaterial = material;
        this.abilityClass =  abilityClass;
    }
}
