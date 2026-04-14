package org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.FirstRoleManager.Knight;
import org.lazberry.xmasLegacy.FirstRoleManager.Rogue;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.XmasLegacy;

public class FirstRoleListener implements Listener {
	private final SkillEffectManager SEM;
	private final XmasLegacy plugin;

	public FirstRoleListener(SkillEffectManager SEM, XmasLegacy plugin) {
		this.SEM = SEM;
		this.plugin = plugin;
	}

	@EventHandler
	public void onSkillUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Knight knight = new Knight(5, 5, SEM, plugin);
		Rogue rogue = new Rogue(4, 4, SEM, plugin);
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemMeta meta = tool.getItemMeta();
		if (meta == null) return;

		NamespacedKey key = new NamespacedKey(plugin, "role_id");
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (pdc != null) {
			if (e.getAction().isLeftClick()) {
				switch (pdc) {
					case "knight" -> knight.useFirstSkill(p);
					case "rogue" -> rogue.useFirstSkill(p);
				}
			} else if (e.getAction().isRightClick()) {
				switch (pdc) {
					case "knight" -> knight.useSecondSkill(p);
					case "rogue" -> rogue.useSecondSkill(p);
				}
			}
		}
	}
}
