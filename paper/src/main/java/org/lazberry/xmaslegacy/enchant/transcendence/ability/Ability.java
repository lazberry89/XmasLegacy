package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Getter
@ConsumableClass
public abstract class Ability {
    public static final NamespacedKey key = KeyUtils.get("ability");
    private final AbilityManager abilityManager;
    private final AbilityType abilityType;
    private final XmasLegacy plugin;

    public Ability(AbilityManager abilityManager, AbilityType abilityType) {
        this.abilityManager = abilityManager;
        this.abilityType = abilityType;
        this.plugin = XmasLegacy.getInstance();
    }

    public boolean isAbilityWeapon(ItemStack item) {
        return KeyUtils.hasKey(item, key, PersistentDataType.STRING, abilityType.name());
    }

    public abstract ItemStack weapon();
    public abstract void execute(Player p);
    public abstract void projectile(Player p);
}
