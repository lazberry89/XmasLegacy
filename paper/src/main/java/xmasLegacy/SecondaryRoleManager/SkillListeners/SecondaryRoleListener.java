package xmasLegacy.SecondaryRoleManager.SkillListeners;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.Listeners;
import xmasLegacy.RoleManager;
import xmasLegacy.SecondaryRoleManager.*;
import xmasLegacy.SecondaryRoleManager.Sniper.BulletType;
import xmasLegacy.SecondaryRoleManager.Sniper.Sniper;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.XmasLegacy;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.*;

@Listeners
public class SecondaryRoleListener implements Listener {
    private final XmasLegacy plugin;
    private final UserManager um;
    private final PartyManager pm;
	private final RoleManager rlm;
	private final SkillEffectManager sem;
	private final SecondRoleManager srm;

    private final Defender defender;
    private final Guardian guardian;
    private final Berserker berserker;
	private final Sniper sniper;

    public SecondaryRoleListener() {
        this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.INSTANCE;
        this.pm = PartyManager.INSTANCE;
		this.rlm = RoleManager.getInstance();
		this.sem = SkillEffectManager.INSTANCE;
		this.srm = SecondRoleManager.INSTANCE;
        this.defender = srm.getRoleInstance(DEFENDER);
	    this.guardian = srm.getRoleInstance(GUARDIAN);
        this.berserker = srm.getRoleInstance(BERSERKER);
		this.sniper = srm.getRoleInstance(SNIPER);
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
		if (!type.equalsIgnoreCase(SecondaryRoles.BERSERKER.name())) return;
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
		if (!type.equalsIgnoreCase(GUARDIAN.name())) return;

		Entity targetEntity = p.getTargetEntity(10, false);
		if (!(targetEntity instanceof LivingEntity le)) return;

		guardian.LinkToTarget(p, le);
	}

	@EventHandler
	public void FighterPassive(EntityDamageByEntityEvent e) {
		if (!(e.getDamageSource().getCausingEntity() instanceof Player p)) return;
		if (!(e.getEntity() instanceof LivingEntity le)) return;
		User user = um.getUser(p.getUniqueId());
		if (user == null) return;
		if (!SecondaryRoles.FIGHTER.equals(user.getRole())) return;

		if (sem.stunMap().contains(e.getEntity().getUniqueId())) {
			e.setDamage(e.getDamage() * 2.0);
			Particle.DustOptions option = new Particle.DustOptions(Color.RED, 0.8f);
			le.getWorld().spawnParticle(Particle.DUST, le.getLocation(), 8, 0.5f, 0.5f, 0.5f, 0.01, option);
			le.getWorld().playSound(le.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
		}
	}

	@EventHandler
	public void SniperShoot(PlayerInteractEvent e) {
		if (!e.getAction().isLeftClick()) return;
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		if (item == null) return;

		if (!(item.getType() == Material.CROSSBOW)) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();
		String key = container.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (key == null || !key.equalsIgnoreCase(SecondaryRoles.SNIPER.name())) return;
		var bullet = sniper.getReloaded(p.getUniqueId());

		Entity target = sniper.shoot(p);
		if (BulletType.SNEAKY.equals(bullet))
			if (target instanceof LivingEntity le)
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));


		p.setCooldown(item, 20);
	}

	@EventHandler
	public void useDash(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		if (!e.getAction().isRightClick()) return;

		User user = um.getUser(p.getUniqueId());
		if (user == null) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();
		String value = container.get(plugin.getNamespacedKey("role_id"), PersistentDataType.STRING);
		if (value == null) return;
		Role role;
		try {
			role = Role.valueOf(value);
		} catch(IllegalArgumentException ex) {
			plugin.getSLF4JLogger().error("Could not find Role \"{}\"", value);
			role = Roles.USER;
		}
		if (role instanceof SecondaryRoles fr) {
			AbstractSecondRole asr = srm.getRoleInstance(fr);
			if (asr == null) return;
			asr.useDash(p);
		}
	}

	@EventHandler
	public void skillUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		PersistentDataContainer container = tool.getItemMeta().getPersistentDataContainer();
		String type = container.get(plugin.getNamespacedKey("emblem_type"), PersistentDataType.STRING);
		String roleS = container.get(plugin.getNamespacedKey("emblem_role"), PersistentDataType.STRING);
		if (type == null || roleS == null) return;
		Role role;
		try {
			role = Role.valueOf(roleS);
		} catch (IllegalArgumentException ex) {
			plugin.getSLF4JLogger().error("Role method 'valueOf(String name)' invoked error. -> \"{}\"", roleS);
			role = null;
		}
		if (role == null) return;
		switch (type) {
			case "range" -> {
				if (role instanceof Roles fr) rlm.getRoleInstance(fr).useSecondSkill(p);
				else if (role instanceof SecondaryRoles sr) rlm.getRoleInstance(sr).useSecondSkill(p);
				else logger(role.name());
			}
			case "target" -> {
				if (role instanceof Roles fr) rlm.getRoleInstance(fr).useFirstSkill(p);
				else if (role instanceof SecondaryRoles sr) rlm.getRoleInstance(sr).useFirstSkill(p);
				else logger(role.name());
			}
			default -> plugin.getSLF4JLogger().error("Emblem type mismatch. Type: {}", type);
		}
	}

	private void logger(@NotNull String role) {
		plugin.getSLF4JLogger().error("Role type mismatch. Role: {}", role);
	}
}
