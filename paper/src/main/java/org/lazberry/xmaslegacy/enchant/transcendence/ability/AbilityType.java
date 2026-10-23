package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum AbilityType {
    SonicHunter(Material.ECHO_SHARD),
    FlameWeaver(Material.FIRE_CHARGE),
    AeroMancer(Material.WIND_CHARGE),
    FrostMancer(Material.BLUE_ICE),
    Dimensionist(Material.ENDER_PEARL);

    private final Material transcendenceMaterial;

    AbilityType(Material material) {
        this.transcendenceMaterial = material;
    }
}
