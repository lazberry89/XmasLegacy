package xmasLegacy.SecondaryRoleManager;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class Berserker extends AbstractSecondRole {
    private final Set<UUID> usedPassive = new HashSet<>();
    private final PartyManager PM;
	private static Berserker instance;

	public static Berserker getInstance() {
		if (instance == null) instance = new Berserker();
		return instance;
	}

    private Berserker() {
        super(SecondaryRoles.BERSERKER);
        this.PM = PartyManager.getInstance();
    }

    @Override
    public void useFirstSkill(Player p) {
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Berserker.getInstance(), emblem, EmblemType.TARGET);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        Vector vector = p.getLocation().getDirection();

        vector.setY(0.3);

        Vector velocity = vector.normalize().multiply(1.6);
        p.setVelocity(velocity);

        p.getWorld().playSound(p, Sound.ENTITY_GHAST_DEATH, 0.8f, 1.1f);
        Particle.DustTransition option = new Particle.DustTransition(Color.RED, Color.BLACK, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, p.getLocation(), 20, 1.4, 1.4, 1.4, 0.01, option);

        GlowUtils.setGlowColor(p, NamedTextColor.RED);
        p.setInvulnerable(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true, false, false));
        // BerserkerSpeedManager.applyFlatSpeed(p);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            BerserkerSpeedManager.removeFlatSpeed(p);
            GlowUtils.clearGlow(p);
            p.setInvulnerable(false);
            p.setHealth(1.0);
        }, 100L);
        p.setCooldown(tool, 50);
    }

    private void fireAxe(Player p) {
        Location startLoc = p.getEyeLocation();

        Vector direction = startLoc.getDirection().clone().normalize();
        direction.add(new Vector(
                (Math.random() - 0.5) * 0.15,
                (Math.random() - 0.5) * 0.15,
                (Math.random() - 0.5) * 0.15
        )).normalize();

        final float playerYaw = p.getLocation().getYaw();

        ArmorStand axeStand = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setMarker(true);
            GlowUtils.setGlowColor(stand, NamedTextColor.DARK_RED);

            Location loc = stand.getLocation();
            loc.setYaw(playerYaw);
            stand.teleport(loc);
            stand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
        });

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;
            final Set<UUID> hit = new HashSet<>();

            @Override
            public void run() {
                if (ticks >= maxTicks || !axeStand.isValid()) {
                    if (axeStand.isValid()) axeStand.remove();
                    this.cancel();
                    return;
                }

                Location currentLoc = axeStand.getLocation().add(direction);
                axeStand.teleport(currentLoc);
                axeStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 2, 0.05, 0.05, 0.05, 0.01);

                double rotation = ticks * 0.6;
                axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));

                for (Entity entity : axeStand.getNearbyEntities(1.1, 1.1, 1.1)) {
                    if (entity instanceof LivingEntity target
                            && !entity.equals(p)
                            && !hit.contains(entity.getUniqueId())) {

                        target.damage(5.0, p);
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS, 60, 1, true, true, false));
                        hit.add(entity.getUniqueId());

                        axeStand.remove();
                        this.cancel();
                        return;
                    }
                }

                if (currentLoc.getBlock().getType().isSolid()) {
                    axeStand.remove();
                    this.cancel();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    @Override
    public void useSecondSkill(Player p) {
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Berserker.getInstance(), emblem, EmblemType.RANGE);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        Location loc = p.getLocation().clone();
        new BukkitRunnable() {
            int shot = 0;

            @Override
            public void run() {
                if (shot >= 3) {
                    this.cancel();
                    return;
                }
                fireAxe(p);
                loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                shot++;
            }
        }.runTaskTimer(getPlugin(), 0L, 6L);

        p.setCooldown(tool, 60);
    }

    @Override
    public void usePassive(Player p) {
        if (this.usedPassive.contains(p.getUniqueId())) return;
        this.usedPassive.add(p.getUniqueId());
        p.getNearbyEntities(2.5 ,2.5, 2.5).stream()
                .filter(e -> e instanceof LivingEntity).map(le -> (LivingEntity) le)
                .filter(e -> !PM.isParty(p.getUniqueId(), e.getUniqueId()))
                .forEach(e -> {
                    Vector dir = e.getLocation().toVector()
                            .subtract(p.getLocation().toVector())
                            .normalize();
                    e.damage(2, p);
                    e.knockback(1.0, -dir.getX(), -dir.getZ());
                });
        // p.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
        p.getWorld().playSound(p, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 25, 0.7, 1.0, 0.7, 0.01);
        GlowUtils.setGlowColor(p, NamedTextColor.DARK_RED);
        p.setInvulnerable(true);
        p.setInvisible(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 2, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
        // BerserkerSpeedManager.applyFlatSpeed(p);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            BerserkerSpeedManager.removeFlatSpeed(p);
            GlowUtils.clearGlow(p);
            p.setInvulnerable(false);
            p.setInvisible(false);
            p.getWorld().playSound(p, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
            p.damage(Integer.MAX_VALUE, p);
        }, 100L);
    }

	public void setAvailable(Player p) {this.usedPassive.remove(p.getUniqueId());}

    public boolean used(@NotNull Player p) {
        return this.usedPassive.contains(p.getUniqueId());
    }

    @Override
    public @NotNull Role getRole() {
        return SecondaryRoles.BERSERKER;
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

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return this.emblem.getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return this.emblem.getRangeEmblem();
	}
}
