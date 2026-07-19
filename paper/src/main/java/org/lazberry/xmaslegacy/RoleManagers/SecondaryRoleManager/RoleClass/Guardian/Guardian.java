package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Guardian;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;

import java.util.*;

@Roles(grade = 2)
public class Guardian extends AbstractSecondRole {
    private final @NotNull PartyManager pm;
    private Container container;

    public static class Container implements RoleContainer {
        public final int cooldown1;
        public final int cooldown2;
        public final @NotNull Map<Player, LivingEntity> targetMap = new HashMap<>();
        public final @NotNull Set<UUID> activeSkill = new HashSet<>();
        public final @NotNull XmasLegacy plugin;

        public Container(@NotNull XmasLegacy plugin, int c1, int c2) {
            this.plugin = plugin;
            this.cooldown1 = c1;
            this.cooldown2 = c2;
        }
    }

    public Guardian() {
        super(SecondaryRoles.GUARDIAN);
        this.pm = PartyManager.INSTANCE;
        this.container = new Container(getPlugin(), 30, 30);
    }

    public @Nullable LivingEntity link(@NotNull Player p) {
        return container.targetMap.get(p);
    }

    public void LinkToTarget(@NotNull Player p, @NotNull LivingEntity target) {
        if (container.targetMap.containsKey(p)) {
            container.targetMap.remove(p);
            p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 타겟과 연결이 해제됨"));
            return;
        }

        container.targetMap.put(p, target);
        String div = target instanceof Player targetP && pm.isParty(p.getUniqueId(), targetP.getUniqueId()) ? "&a아군&f" : "&c적군&f";
        InfoUtils.info(p, div + " 타겟과 연결됨");
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!container.targetMap.containsKey(p) || !target.isValid() || !p.isOnline()) {
                    container.targetMap.remove(p);
                    p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " &c타겟과 연결이 끊어짐"));
                    p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 0.3f, 1.3f);
                    this.cancel();
                    return;
                }

                Location from = p.getLocation().add(0, 1, 0);
                Location to = target.getEyeLocation();

                RayTraceResult ray = p.getWorld().rayTraceBlocks(
                        from,
                        to.toVector().subtract(from.toVector()).normalize(),
                        from.distance(to)
                );
                if (ray != null && ray.getHitBlock() != null) {
                    container.targetMap.remove(p);
                    p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " &c장애물로 인해 연결이 끊어짐"));
                    this.cancel();
                    return;
                }
                boolean isAlly = pm.isParty(p.getUniqueId(), target.getUniqueId());

                drawBeam(from, to, isAlly);
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    private void drawBeam(Location from, Location to, boolean isAlly) {
        Particle.DustTransition allyTrans = new Particle.DustTransition(Color.GREEN, Color.WHITE, 0.5f);
        Particle.DustTransition enemyTrans = new Particle.DustTransition(Color.RED, Color.YELLOW, 0.5f);

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d < distance; d += 0.3) {
            Location point = from.clone().add(direction.clone().multiply(d));
            from.getWorld().spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    point,
                    1,
                    0, 0, 0,
                    0.5,
                    isAlly ? allyTrans : enemyTrans
            );
        }
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.TARGET_GUARD), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.OVERCHARGE_PRISM), container, 30);
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.IRON_SPEAR)
                .setName(ColorUtils.chat("&8&l가디언의 창"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole()
                )
                .hideAllFlags()
                .addEnchant(Enchantment.LUNGE, 2)
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.DIAMOND_HELMET)
                .setName(ColorUtils.chat("&b&l마계의 오래된 갑옷"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .hideAllFlags()
                .build().clone();
    }
}
