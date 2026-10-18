package org.lazberry.xmaslegacy.enchant.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class ArmorDamageListener implements Listener {
	private final EnchantManager enchantManager;

	@Inject
	public ArmorDamageListener(EnchantManager enchantManager) {
		this.enchantManager = enchantManager;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;

		EntityDamageEvent.DamageCause cause = event.getCause();
		if (cause == EntityDamageEvent.DamageCause.FREEZE ||
				cause == EntityDamageEvent.DamageCause.FALL ||
				cause == EntityDamageEvent.DamageCause.POISON ||
				cause == EntityDamageEvent.DamageCause.STARVATION) {
			return;
		}

		double totalReduction = 0.0;

		for (ItemStack armor : player.getInventory().getArmorContents()) {
			if (armor == null || armor.getType().isAir()) continue;

			// PDC에서 강화 레벨을 읽어옴
			Integer lvl = enchantManager.getEnchantLevel(armor);
			if (lvl == null || lvl <= 0) continue;

			// 실시간으로 EnchantManager의 계산 메서드 호출
			double reduction = enchantManager.calculateArmorReduction(armor.getType(), lvl);
			totalReduction += reduction;
		}

		if (totalReduction > 0.0) {
			totalReduction = Math.min(totalReduction, 95.0);

			double originalDamage = event.getDamage();
			double finalDamage = originalDamage * (1.0 - (totalReduction / 100.0));

			event.setDamage(finalDamage);
		}
	}
}
