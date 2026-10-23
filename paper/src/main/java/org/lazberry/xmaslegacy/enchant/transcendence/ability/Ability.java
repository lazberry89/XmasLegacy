package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;

@Getter
@ConsumableClass
public abstract class Ability {
    public static final NamespacedKey key = KeyUtils.get("ability");
	private final EnchantManager enchantManager;
    private final AbilityType abilityType;
    private final XmasLegacy plugin;

    public Ability(EnchantManager enchantManager, AbilityType abilityType) {
	    this.enchantManager = enchantManager;
	    this.abilityType = abilityType;
        this.plugin = XmasLegacy.getInstance();
    }

    public static boolean isAbilityWeapon(ItemStack item) {
        return KeyUtils.hasKey(item, key);
    }

	public static boolean isAbilityWeapon(ItemStack item, AbilityType abilityType) {
		return KeyUtils.hasKey(item, key, PersistentDataType.STRING, abilityType.name());
	}

	public static AbilityType getAbilityByItem(ItemStack item) {
		return ParseEnum.of(AbilityType.class).parse(KeyUtils.get(item, key, PersistentDataType.STRING));
	}

    public abstract ItemStack weapon();
    public abstract void execute(Player p);
    public abstract void projectile(Player p);
}
