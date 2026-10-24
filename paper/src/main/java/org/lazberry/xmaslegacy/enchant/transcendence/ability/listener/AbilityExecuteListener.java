package org.lazberry.xmaslegacy.enchant.transcendence.ability.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class AbilityExecuteListener implements Listener {
	private final AbilityManager abilityManager;

	@Inject
	public AbilityExecuteListener(AbilityManager abilityManager) {
		this.abilityManager = abilityManager;
	}

	@EventHandler
	public void onAbilityUse(PlayerInteractEvent e) {
		if (!e.getAction().isRightClick()) return;

		var player = e.getPlayer();
		var item = player.getInventory().getItemInMainHand();

		if (item.getType().isAir()) return;
		if (!Ability.isAbilityWeapon(item)) {
			return;
		}

		if (player.getCooldown(item) > 0) return;
		AbilityType type = Ability.getAbilityByItem(item);
		if (type == null) {
			InfoUtils.error(player, "타입을 찾을 수 없습니다.");
			return;
		}

		Ability ability = abilityManager.getAbility(type);
		if (ability == null) {
			InfoUtils.error(player, "서버에 등록된 초월 능력이 아닙니다.");
			return;
		}

		boolean result = false;
		if (player.isSneaking())
			result = ability.projectile(player);
		else result = ability.execute(player);
		if (result) player.setCooldown(item, 20);
	}
}
