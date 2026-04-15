package org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.FirstRoleManager.Archer;
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
        Archer archer = new Archer(4, 4, plugin);
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
                    case "archer" -> archer.useFirstSkill(p);
				}
			} else if (e.getAction().isRightClick()) {
				switch (pdc) {
					case "knight" -> knight.useSecondSkill(p);
					case "rogue" -> rogue.useSecondSkill(p);
                    case "archer" -> archer.useSecondSkill(p);
				}
			}
		}
	}
    //Archer skill
    @EventHandler
    public void onShockHit(ProjectileHitEvent e) {
        NamespacedKey key =  new NamespacedKey(plugin, "skill");
        String npKey = e.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        Projectile projectile = e.getEntity();
        if (!(projectile.getShooter() instanceof Player) && !(projectile instanceof Arrow a)) return;
        if (!(npKey != null && npKey.equals("archer_arrow"))) return;
        if (e.getHitBlock() != null) {
            Location loc = e.getHitBlock().getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
            loc.getWorld().createExplosion(loc, 4,  false, false);
            return;
        }

        if (e.getHitEntity() instanceof LivingEntity victim) {
            Location loc = victim.getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
            loc.getWorld().createExplosion(loc, 2,  false, false);
        }



    }
}
