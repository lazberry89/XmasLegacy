package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
@Registry.Exclude(type = ServerType.LOBBY)
public class AbilityManager {
	private final Map<AbilityType, Ability> abilities = new HashMap<>();

	@Inject
	public AbilityManager(EnchantManager enchantManager, XmasLegacy plugin) {
		Arrays.stream(AbilityType.values()).forEach(type -> {
			try {
				var instance = type.getAbilityClass().getDeclaredConstructor(EnchantManager.class, XmasLegacy.class)
						.newInstance(enchantManager, plugin);
				abilities.put(type, instance);
			} catch (Exception e) {
				log.error("Failed to register ability class instance to map.", e);
			}
		});
	}

	public <V extends Ability> V getAbility(AbilityType abilityType) {
		return (V) abilities.get(abilityType);
	}
}
