package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;

@Slf4j
@Getter
@ConsumableClass
public abstract class Ability {
    public static final NamespacedKey key = KeyUtils.get("ability");

	private final EnchantManager enchantManager;
    private final AbilityType abilityType;
    private final XmasLegacy plugin;

    public Ability(EnchantManager enchantManager, XmasLegacy plugin, AbilityType abilityType) {
	    this.enchantManager = enchantManager;
	    this.abilityType = abilityType;
        this.plugin = plugin;
    }

    public static boolean isAbilityWeapon(ItemStack item) {
        return KeyUtils.hasKey(item, key);
    }

	public static boolean isAbilityWeapon(ItemStack item, AbilityType abilityType) {
		return KeyUtils.hasKey(item, key, PersistentDataType.STRING, abilityType.name());
	}

	public static AbilityType getAbilityByItem(ItemStack item) {
		String value = KeyUtils.get(item, key, String.class);
		return ParseEnum.of(AbilityType.class).parse(value);
	}

    public abstract ItemStack weapon();
    public abstract boolean execute(Player p);
    public abstract boolean projectile(Player p);
}
