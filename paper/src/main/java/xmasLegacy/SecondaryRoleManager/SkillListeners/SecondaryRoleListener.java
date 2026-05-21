package xmasLegacy.SecondaryRoleManager.SkillListeners;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.SecondarySkill;
import xmasLegacy.SecondaryRoleManager.Berserker;
import xmasLegacy.SecondaryRoleManager.Defender;
import xmasLegacy.SecondaryRoleManager.Guardian;
import xmasLegacy.XmasLegacy;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.*;

@SuppressWarnings("DuplicatedCode, unused")
public class SecondaryRoleListener implements Listener {
    private final XmasLegacy plugin;
    private final UserManager um;
    private final PartyManager pm;
    private final Defender defender;
    private final Guardian guardian;
    private final Berserker berserker;

    public SecondaryRoleListener() {
        this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.getInstance();
        this.pm = PartyManager.getInstance();
        this.defender = plugin.defender;
        this.guardian = plugin.guardian;
        this.berserker = plugin.berserker;
    }

    @EventHandler
    public void defenderPassive(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;

        Role role = user.getRole();
        if (!DEFENDER.equals(role)) return;

        long partyCount = p.getNearbyEntities(3, 3, 3).stream()
                .filter(n -> n instanceof LivingEntity)
                .filter(n -> n instanceof Player ally && pm.isParty(p.getUniqueId(), ally.getUniqueId()))
                .count();
        if (partyCount > 0) {
            double reductionPercent = partyCount * 0.07;

            double originalDamage = e.getDamage();
            double finalDamage = originalDamage * (1.0 - reductionPercent);

            e.setDamage(finalDamage);
        }
    }

    @EventHandler
    public void guardianPassive(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        for (Player guardians : Bukkit.getOnlinePlayers()) {
            User user = um.getUser(guardians.getUniqueId());
            if (user == null) continue;
            if (!(GUARDIAN.equals(user.getRole()))) continue;

            LivingEntity linked = guardian.link(guardians);
            if (linked == null || !linked.equals(victim)) continue;

            boolean isAlly = linked instanceof Player t
                    && pm.isParty(guardians.getUniqueId(), t.getUniqueId());

            if (isAlly) {
                double absorbed = e.getDamage() * 0.2;
                e.setDamage(e.getDamage() - absorbed);
                if (!guardians.equals(victim)) {
                    guardians.damage(absorbed);
                }
            } else {
                double lifeSteal = e.getFinalDamage() * 0.1;
                AttributeInstance maxHealth = guardians.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth == null) continue;
                guardians.setHealth(Math.min(
                        guardians.getHealth() + lifeSteal,
                        maxHealth.getValue()
                ));
            }
        }
    }

	@EventHandler
	public void berserkerPassive(PlayerDeathEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		String type = pdc.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (type == null) return;
		if (!type.equalsIgnoreCase("berserker")) return;
		if (berserker.used(p)) {
			berserker.setAvailable(p);
			return;
		}

		e.setCancelled(true);
		p.setHealth(2.0);
		berserker.usePassive(p);
	}

	@EventHandler
	public void guardianLink(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		User user = um.getUser(p.getUniqueId());
		if (user == null) return;

		if (!e.getAction().isLeftClick()) return;


		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		String type = pdc.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (type == null) return;
		if (!type.equalsIgnoreCase("guardian")) return;

		Entity targetEntity = p.getTargetEntity(10, false);
		if (!(targetEntity instanceof LivingEntity le)) return;

		guardian.LinkToTarget(p, le);
	}

	@EventHandler
	public void skillChange(PlayerToggleSneakEvent e) {
		if (e.isSneaking()) return;
		Player p = e.getPlayer();
		User user = um.getUser(p.getUniqueId());
		if (user == null) return;

		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		String type = pdc.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (type == null) return;

		e.setCancelled(true);
		switch (type) {
			case "defender" -> {
				defender.next(p);
				p.sendActionBar(ColorUtils.chat(defender.getCurrentSkill(p).getSkillName()));
			}
			case "guardian" -> {
				guardian.next(p);
				p.sendActionBar(ColorUtils.chat(guardian.getCurrentSkill(p).getSkillName()));
			}
			case "berserker" -> {
				berserker.next(p);
				p.sendActionBar(ColorUtils.chat(berserker.getCurrentSkill(p).getSkillName()));
			}
		}
	}

	@EventHandler
	public void skillUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		User user = um.getUser(p.getUniqueId());
		if (user == null) return;

		if (!e.getAction().isRightClick()) return;

		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		String type = pdc.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (type == null) return;
		e.setCancelled(true);
		switch (type) {
			case "defender" -> {
				if (defender.getCurrentSkill(p) == null) return;
				if (defender.getCurrentSkill(p) == SecondarySkill.MAGNETIC_FIELD) {
					defender.useFirstSkill(p);
				} else {
					defender.useSecondSkill(p);
				}
			}
			case "guardian" -> {
				if (guardian.getCurrentSkill(p) == null) return;
				if (guardian.getCurrentSkill(p) == SecondarySkill.TARGET_GUARD) {
					guardian.useFirstSkill(p);
				} else {
					guardian.useSecondSkill(p);
				}
			}
			case "berserker" -> {
				if (berserker.getCurrentSkill(p) == null) return;
				if (berserker.getCurrentSkill(p) == SecondarySkill.MADNESS) {
					berserker.useFirstSkill(p);
				} else {
					berserker.useSecondSkill(p);}
			}
		}
		p.swingMainHand();
		p.playSound(p, "xmaslegacy:skill_use", 1.0f, 1.0f);
	}

}
