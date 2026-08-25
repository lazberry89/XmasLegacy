package org.lazberry.xmaslegacy.role.passive.listeners;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.exp.ExpManager;
import org.lazberry.xmaslegacy.role.general.RoleManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.concurrent.ThreadLocalRandom;

@Listeners
@Registry.Include(type = ServerType.WILD)
public class KnightPassiveListener extends PassiveListeners implements Listener {
    private final RoleManager rm;
    private final ExpManager em;

    @Inject
    public KnightPassiveListener(UserManager um, RoleManager rm, ExpManager em) {
        super(ServerRoles.KNIGHT, um);
        this.rm = rm;
        this.em = em;
    }

    @EventHandler
    public void critWhenAttack(PrePlayerAttackEntityEvent e) {
        Player p = e.getPlayer();
        Entity entity = e.getAttacked();

        if (entity instanceof Player) return;

        if (entity instanceof LivingEntity victim)
            canUsePassive(p, u -> {
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    if (victim.isValid() || !victim.isDead()) {
                        victim.damage(ThreadLocalRandom.current().nextInt(3, 6), p);
                        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation(), 10, 0.2, 0.2, 0.2, 0.01);
                        p.sendActionBar(ColorUtils.chat("&6&lCRIT!"));
                        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                }
            });
    }

    @EventHandler
    public void whenKillEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();

        if (!(damager instanceof Player player)) return;
        if (!(victim instanceof LivingEntity le)) return;

        if (!(victim instanceof Monster)) return;

        if (le.getHealth() <= e.getDamage()) {
            canUsePassive(player, u -> {
                int expToGive;
                int moneyToGive;

                switch (victim.getType()) {
                    case ENDER_DRAGON, WITHER -> {
                        expToGive = ThreadLocalRandom.current().nextInt(150, 301);
                        moneyToGive = ThreadLocalRandom.current().nextInt(10000, 30001);
                    }
                    case WARDEN -> {
                        expToGive = ThreadLocalRandom.current().nextInt(100, 201);
                        moneyToGive = ThreadLocalRandom.current().nextInt(5000, 15001);
                    }
                    case BLAZE, WITHER_SKELETON, RAVAGER -> {
                        expToGive = ThreadLocalRandom.current().nextInt(20, 36);
                        moneyToGive = ThreadLocalRandom.current().nextInt(500, 1201);
                    }
                    case ENDERMAN, WITCH, GHAST -> {
                        expToGive = ThreadLocalRandom.current().nextInt(8, 16);
                        moneyToGive = ThreadLocalRandom.current().nextInt(200, 501);
                    }
                    default -> {
                        expToGive = ThreadLocalRandom.current().nextInt(2, 6);
                        moneyToGive = ThreadLocalRandom.current().nextInt(50, 151);
                    }
                }

                em.addRoleExp(player, expToGive);
                u.addDollars(moneyToGive);

                sendExpAlert(player, expToGive);
                InfoUtils.info(player, "&a+{}원 &7({} 처치)", moneyToGive, victim.getName());
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
            });
        }
    }
}
