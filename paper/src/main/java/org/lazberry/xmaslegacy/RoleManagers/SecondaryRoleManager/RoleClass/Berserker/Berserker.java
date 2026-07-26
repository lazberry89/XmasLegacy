package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Berserker;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Roles(grade = 2)
@Registry.Exclude(type = ServerType.LOBBY)
public class Berserker extends AbstractSecondRole {
    private final @NotNull Set<UUID> usedPassive = new HashSet<>();
    private final @NotNull PartyManager pm;
    private final Container container;

    public record Container(
            XmasLegacy plugin
    ) implements RoleContainer {}

	@Inject
    public Berserker(@NotNull PartyManager pm) {
        super(SecondaryRoles.BERSERKER);
        this.pm = pm;
        this.container = new Container(getPlugin());
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.ULTRA_MADNESS), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.TRIPLE_TOMAHAWK), container, 30);
    }

    @Override
    public void usePassive(@NotNull Player p) {
        if (this.usedPassive.contains(p.getUniqueId())) return;
        this.usedPassive.add(p.getUniqueId());
        explode(p);
        apply(p);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            reset(p);
            p.setHealth(0);
        }, 100L);
    }

    public void explode(@NotNull Player p) {
        p.getNearbyEntities(2.5 ,2.5, 2.5).stream()
                .filter(e -> e instanceof LivingEntity).map(le -> (LivingEntity) le)
                .filter(e -> !pm.isParty(p.getUniqueId(), e.getUniqueId()))
                .forEach(e -> {
                    Vector dir = e.getLocation().toVector()
                            .subtract(p.getLocation().toVector())
                            .normalize();
                    e.damage(2, p);
                    e.knockback(1.0, -dir.getX(), -dir.getZ());
                });
    }

    private void apply(@NotNull Player p) {
        p.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
        p.getWorld().playSound(p, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 25, 0.7, 1.0, 0.7, 0.01);
        GlowUtils.glow(p, NamedTextColor.DARK_RED);
        p.setInvulnerable(true);
        p.setInvisible(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 2, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
    }

    public void reset(@NotNull Player p) {
        GlowUtils.clearGlow(p);
        p.setInvulnerable(false);
        p.setInvisible(false);
        p.getWorld().playSound(p, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
    }

	public void setAvailable(@NotNull Player p) {this.usedPassive.remove(p.getUniqueId());}

    public boolean used(@NotNull Player p) {
        return this.usedPassive.contains(p.getUniqueId());
    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.IRON_SWORD)
		        .setName(ColorUtils.chat("&7&l단단한 철검"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setRoleDefault(this.getRole())
		        .hideAllFlags()
		        .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_BOOTS)
		        .setName(ColorUtils.chat("&7&l재빠른 신발"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setRoleDefault(this.getRole())
		        .hideAllFlags()
		        .build().clone();
    }
}
