package org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.FirstRoleManager.Archer;
import org.lazberry.xmasLegacy.FirstRoleManager.Knight;
import org.lazberry.xmasLegacy.FirstRoleManager.Rogue;
import org.lazberry.xmasLegacy.FirstRoleManager.Warrior;
import org.lazberry.xmasLegacy.Skill.BasicSkills;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;
import org.lazberry.xmasLegacy.XmasLegacy;

public class FirstRoleListener implements Listener {
	private final SkillEffectManager SEM;
	private final XmasLegacy plugin;
	private final Knight knight;
	private final Rogue rogue;
	private final Archer archer;
	private final Warrior warrior;


	public FirstRoleListener(SkillEffectManager SEM, XmasLegacy plugin, Knight knight, Rogue rogue, Archer archer, Warrior warrior) {
		this.SEM = SEM;
		this.plugin = plugin;
		this.knight = knight;
		this.rogue = rogue;
		this.archer = archer;
		this.warrior = warrior;
	}


	@EventHandler
	public void onSkillUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemMeta meta = tool.getItemMeta();
		if (meta == null) return;

		NamespacedKey key = new NamespacedKey(plugin, "role_id");
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (pdc != null) {
			if (e.getAction().isLeftClick()) {
				switch (pdc) {
					case "knight" -> {
						if (knight.getCurrentSkill() == null) return;
						if (knight.getCurrentSkill() == BasicSkills.SHARP_SWEEPING) {
							knight.useFirstSkill(p);
						} else if (knight.getCurrentSkill() == BasicSkills.TAUNT) {
							knight.useSecondSkill(p);
						}
					}
					case "rogue" -> {
						if (rogue.getCurrentSkill() == null) return;
						if (rogue.getCurrentSkill() == BasicSkills.DAGGER_RUSH) {
							rogue.useFirstSkill(p);
						} else if (rogue.getCurrentSkill() == BasicSkills.SMOKE) {
							rogue.useSecondSkill(p);
						}
					}
					case "archer" -> {
						if (archer.getCurrentSkill() == null) return;
						if (archer.getCurrentSkill() == BasicSkills.SHOCK_DART) {
							archer.useFirstSkill(p);
						} else if (archer.getCurrentSkill() == BasicSkills.BACK_DASH) {
							archer.useSecondSkill(p);
						}
					}
					case "warrior" -> {
						if (warrior.getCurrentSkill() == null) return;
						if (warrior.getCurrentSkill() == BasicSkills.BLOOD_FRENZY) {
							warrior.useFirstSkill(p);
						} else if (warrior.getCurrentSkill() == BasicSkills.TOMAHAWK) {
							warrior.useSecondSkill(p);
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onSkillChange(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemMeta meta = tool.getItemMeta();
		if (meta == null) return;

		e.setCancelled(true);

		NamespacedKey key = new NamespacedKey(plugin, "role_id");
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (pdc != null) {
			switch (pdc) {
				case "knight" -> {
					knight.next();
					p.sendActionBar(ComponentChanger.comp("&8&l" + knight.getCurrentSkill().getSkillName()));
				}
				case "rogue" -> {
					rogue.next();
					p.sendActionBar(ComponentChanger.comp("&8&l" + rogue.getCurrentSkill().getSkillName()));
				}
				case "archer" -> {
					archer.next();
					p.sendActionBar(ComponentChanger.comp("&8&l" + archer.getCurrentSkill().getSkillName()));
				}
				case "warrior" -> {
					warrior.next();
					p.sendActionBar(ComponentChanger.comp("&8&l" + warrior.getCurrentSkill().getSkillName()));
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
            loc.getWorld().strikeLightning(loc);
            return;
        }

        if (e.getHitEntity() instanceof LivingEntity victim) {
            Location loc = victim.getLocation();
	        loc.getWorld().strikeLightning(loc);
        }



    }
}
