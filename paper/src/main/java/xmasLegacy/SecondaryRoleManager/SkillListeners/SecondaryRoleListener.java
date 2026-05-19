package xmasLegacy.SecondaryRoleManager.SkillListeners;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.SecondaryRoleManager.Berserker;
import xmasLegacy.SecondaryRoleManager.Defender;
import xmasLegacy.SecondaryRoleManager.Guardian;
import xmasLegacy.XmasLegacy;

public class SecondaryRoleListener implements Listener {
    private final XmasLegacy plugin;
    private final UserManager um;
    private final PartyManager pm;
    private final Defender defender;
    private final Guardian guardian;
    private final Berserker berserker;

    public SecondaryRoleListener(XmasLegacy plugin) {
        this.plugin = plugin;
        this.um = plugin.UM;
        this.pm = plugin.PM;
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
        if (!SecondaryRoles.DEFENDER.equals(role)) return;

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
            if (!(SecondaryRoles.GUARDIAN.equals(user.getRole()))) continue;

            LivingEntity linked = guardian.link(guardians);
            if (linked == null || !linked.equals(victim)) continue;

            boolean isAlly = linked instanceof Player t
                    && pm.isParty(guardians.getUniqueId(), t.getUniqueId());

            if (isAlly) {
                double absorbed = e.getDamage() * 0.2;
                e.setDamage(e.getDamage() - absorbed);
                if (!guardians.equals(victim)) { // Guardian이 본인 링크한 경우 방지
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
    public void berserkerPassive(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;
        if (!SecondaryRoles.BERSERKER.equals(user.getRole())) return;
        if (berserker.used(p)) return;

        if (p.getHealth() - e.getFinalDamage() <= 0) {
            e.setCancelled(true); // 피해 취소
            berserker.usePassive(p);
        }
    }
}
