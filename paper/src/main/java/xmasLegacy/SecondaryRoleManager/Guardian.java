package xmasLegacy.SecondaryRoleManager;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.InfoLevel;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;

@SuppressWarnings("DuplicatedCode, unused")
public class Guardian extends AbstractSecondRole {
    private final @NotNull PartyManager pm;
    private final @NotNull Map<Player, LivingEntity> targetMap = new HashMap<>();
    private final @NotNull Set<UUID> activeSkill = new HashSet<>();

    public Guardian() {
        super(SecondaryRoles.GUARDIAN);
        this.pm = PartyManager.INSTANCE;
    }

    public @Nullable LivingEntity link(Player p) {
        return this.targetMap.get(p);
    }

    public void LinkToTarget(@NotNull Player p, @NotNull LivingEntity target) {
        if (targetMap.containsKey(p)) {
            targetMap.remove(p);
            p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 타겟과 연결이 해제됨"));
            return;
        }

        targetMap.put(p, target);
        String div = target instanceof Player targetP && pm.isParty(p.getUniqueId(), targetP.getUniqueId()) ? "&a아군&f" : "&c적군&f";
        getPlugin().infoMsg(InfoLevel.INFO, p, div + " 타겟과 연결됨");
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!targetMap.containsKey(p) || !target.isValid() || !p.isOnline()) {
                    targetMap.remove(p);
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
                    targetMap.remove(p);
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
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.TARGET);
        Bukkit.getPluginManager().callEvent(skillUse);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        LivingEntity target = targetMap.get(p);
        if (target == null) {
            getPlugin().infoMsg(InfoLevel.ERROR, p, "연결된 타겟이 없습니다!");
            return;
        }
        if (activeSkill.contains(p.getUniqueId())) {
            activeSkill.remove(p.getUniqueId());
            p.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
            p.setCooldown(tool, 60);
            return;
        }

        activeSkill.add(p.getUniqueId());
        p.sendActionBar(ColorUtils.chat("&a스킬 활성화"));
        boolean isAlly = target instanceof Player t && pm.isParty(p.getUniqueId(), t.getUniqueId());

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!activeSkill.contains(p.getUniqueId())
                        || !targetMap.containsKey(p)
                        || !target.isValid()
                        || !p.isOnline()) {
                    activeSkill.remove(p.getUniqueId());
                    if (isAlly) {
                        target.removePotionEffect(PotionEffectType.RESISTANCE);
                        target.removePotionEffect(PotionEffectType.REGENERATION);
                    } else {
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                    }
                    p.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                    this.cancel();
                    p.setCooldown(tool, 60);
                    return;
                }


                if (ticks % 20 == 0) {
                    if (ticks >= 80) {
                        activeSkill.remove(p.getUniqueId());
                        this.cancel();
                        p.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                        p.setCooldown(tool, 60);
                        return;
                    }
                    if (!isAlly) {
                        target.damage(3, p);
                    }
                    if (!consumeEnergy(p, 2)) {
                        activeSkill.remove(p.getUniqueId());
                        this.cancel();
                        getPlugin().infoMsg(InfoLevel.ERROR, p, "에너지가 모두 소모되었습니다.");
                        p.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                        p.setCooldown(tool, 60);
                        return;
                    }
                }

                if (isAlly) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2, 1, true, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 1, true, false, false));
                } else {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, 1, true, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2, 1, true, false, false));
                }
                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
        Bukkit.getPluginManager().callEvent(skillUse);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }

        LivingEntity target = targetMap.get(p);
        if (target == null) {
            getPlugin().infoMsg(InfoLevel.ERROR, p, "연결된 타겟이 없습니다!");
            return;
        }
        if (!consumeEnergy(p, 4)) return;

        Location center = target.getLocation();

        Vector[] directions = {
                new Vector( 1, 0,  1).normalize(),
                new Vector( 1, 0, -1).normalize(),
                new Vector(-1, 0,  1).normalize(),
                new Vector(-1, 0, -1).normalize()
        };

        Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            double distance = 0;
            final double maxDistance = 12.0;

            @Override
            public void run() {
                if (distance >= maxDistance) {
                    this.cancel();
                    return;
                }

                for (Vector dir : directions) {
                    Location point = center.clone().add(dir.clone().multiply(distance));

                    point.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 1, 0, 0, 0, 0);
                    point.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0.1, 0.1, 0.1, 0);

                    point.getWorld().playSound(point, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.5f); // 볼륨 낮게
                    point.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, point, 1, 0, 0, 0, 0);

                    for (Entity e : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                        if (e instanceof LivingEntity le
                                && !e.equals(p)
                                && !pm.isParty(p.getUniqueId(), e.getUniqueId())
                                && !hitEntities.contains(e.getUniqueId())) {
                            le.damage(8.0, p);
                            hitEntities.add(e.getUniqueId());
                        }
                    }
                }
                distance += 1.5;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);

        p.setCooldown(tool, 60);
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull Role getRole() {
        return SecondaryRoles.GUARDIAN;
    }

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

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return emblem.getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return emblem.getRangeEmblem();
	}
}
