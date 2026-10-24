package org.lazberry.xmaslegacy.enchant.transcendence.ability.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.logic.FlameWeaver;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class FlameWeaverListener implements Listener {
	private final FlameWeaver flameWeaver;

	@Inject
	public FlameWeaverListener(AbilityManager abilityManager) {
		this.flameWeaver = abilityManager.getAbility(AbilityType.FLAME_WEAVER);
	}

	@EventHandler
	public void whenHit(ProjectileHitEvent e) {
		flameWeaver.whenHit(e);
	}
}
