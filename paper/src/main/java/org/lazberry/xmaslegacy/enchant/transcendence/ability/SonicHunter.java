package org.lazberry.xmaslegacy.enchant.transcendence.ability;

import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.*;

import java.util.Objects;
import java.util.function.Consumer;

@Registry.Exclude(type = ServerType.LOBBY)
public class SonicHunter extends Ability {
    public static final NamespacedKey feared = KeyUtils.get("feared");

    @Inject
    public SonicHunter(EnchantManager enchantManager) {
        super(enchantManager, AbilityType.SonicHunter);
    }

    @Override
    public ItemStack weapon() {
        var builder = OraxenItems.getItemById("echo_reaver");
        ItemStack item = builder == null ?
                new ItemStack(getAbilityType().getTranscendenceMaterial()) : builder.build();
		getEnchantManager().setEnchantable(item);
		getEnchantManager().applyLevelChange(item, 10);
        return ItemBuilder.of(getPlugin(), item)
                .name(ColorUtils.chat("&#0021E1&lE&#0B34E2&lc&#1647E3&lh&#215BE4&lo&#2C6EE5&lR" +
                        "&#3681E5&le&#4194E6&la&#4CA8E7&lv&#57BBE8&le&#62CEE9&lr"))
                .lore(EnchantManager.PRISM,
                        ColorUtils.chat("&7초월함: SonicHunter"))
                .setTag(key, PersistentDataType.STRING, getAbilityType().name())
                .hideAllFlags()
                .setGlint(true)
                .build();
    }

    @Override
    public void execute(Player p) {
        var target = p.getTargetEntity(15, true);
        if (!(target instanceof LivingEntity lv)) return;

        lv.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,
                20 * 5, 2, true, false, false));
        lv.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                20 * 5, 1, true, false, false));
        lv.getWorld().playSound(lv, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.0f);
        KeyUtils.set(lv, feared, (byte) 1);
        GlowUtils.glow(lv, NamedTextColor.DARK_AQUA);

        fearedParticleEffect(lv);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            KeyUtils.remove(lv, feared);
            GlowUtils.clearGlow(lv);
        }, 20 * 5);
    }

    public boolean isFeared(LivingEntity lv) {
        return KeyUtils.hasKey(lv, feared);
    }

    private void fearedParticleEffect(LivingEntity target) {
        new BukkitRunnable() {
            int tick = 20;

            @Override
            public void run() {
                var loc = target.getLocation();
                loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0),
                        7, 0.7, 0.7, 0.7, 0.01);
                if (--tick <= 0 || !target.isValid()) {
                    this.cancel();
                }
            }

        }.runTaskTimer(getPlugin(), 0L, 5L);
    }

    @Override
    public void projectile(Player p) {
        stunNearbyWhenProjectileExecuted(p);
        shootInstantSonicBoom(p);
    }

    private void shootInstantSonicBoom(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();

        player.getWorld().playSound(start, org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);

        for (double d = 0; d <= 30.0; d += 0.7) {
            Location loc = start.clone().add(dir.clone().multiply(d));
            onHitSonicBoom(player, loc);
            loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1, 0, 0, 0, 0);
        }
    }

    private void onHitSonicBoom(Player executor, Location loc) {
        loopAroundEntities(executor, loc, 1, lv -> {
            lv.damage(10, executor);
            Vector vector = loc.getDirection().subtract(executor.getVelocity());
            vector.normalize();
            vector.multiply(1.2);

            executor.setVelocity(vector);
        });
    }

    private void stunNearbyWhenProjectileExecuted(Player executor) {
        loopAroundEntities(executor, executor.getLocation(), 5, l -> {
            StunUtils.stun(l.getUniqueId(), 20 * 3L, "청각손실");
            l.damage(5);
        });
    }

    private void loopAroundEntities(Player executor, Location loc, double range, Consumer<LivingEntity> action) {
        var affected = loc.getNearbyLivingEntities(range, range,
                l -> !Objects.equals(l.getUniqueId(), executor.getUniqueId()));
        if (affected.isEmpty()) return;
        affected.forEach(action);
    }
}
