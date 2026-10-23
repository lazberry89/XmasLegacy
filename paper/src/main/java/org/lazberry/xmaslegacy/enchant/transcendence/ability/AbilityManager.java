package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Registry.Exclude(type = ServerType.LOBBY)
public class AbilityManager {
	private final Map<AbilityType, Ability> abilities = new HashMap<>();

	@Inject
	public AbilityManager(SonicHunter sonicHunter) {
		abilities.put(AbilityType.SonicHunter, sonicHunter);
	}

	public <V extends Ability> V getAbility(AbilityType abilityType) {
		return (V) abilities.get(abilityType);
	}
}
