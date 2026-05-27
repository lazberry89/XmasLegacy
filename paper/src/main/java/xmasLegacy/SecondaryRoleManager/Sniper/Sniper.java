package xmasLegacy.SecondaryRoleManager.Sniper;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
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
import xmasLegacy.SecondaryRoleManager.AbstractSecondRole;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;

@SuppressWarnings("DuplicatedCode, unused, FieldCanBeLocal")
public class Sniper extends AbstractSecondRole {
    private final PartyManager pm;
    private final SkillEffectManager sem;
    private final Map<UUID, BulletType> reloaded = new HashMap<>();
    private final Map<UUID, Integer> dashCount = new HashMap<>();
    private final Set<UUID> isReloading = new HashSet<>();
    private final Set<UUID> magicalBullet = new HashSet<>();
    private final Map<UUID, BulletType> lastHitRecord = new HashMap<>();

    private static Sniper instance;

    public BulletType getLastHitType(Entity entity) {
        return lastHitRecord.get(entity.getUniqueId());
    }

    public static Sniper getInstance() {
        if (instance == null) instance = new Sniper();
        return instance;
    }

    private Sniper() {
        super(SecondaryRoles.SNIPER);
        this.pm = PartyManager.getInstance();
        this.sem = SkillEffectManager.getInstance();
    }

    @Override
    public void useFirstSkill(Player p) {
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Sniper.getInstance(), emblem, EmblemType.TARGET);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        UUID uuid = p.getUniqueId();
        if (isReloading.contains(uuid)) {
            getPlugin().infoMsg(InfoLevel.WARN, p, "이미 장전중입니다!");
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        BulletType bullet = selectBullet();

        p.sendActionBar(ColorUtils.chat("&a장전중..."));
        this.isReloading.add(p.getUniqueId());
        p.getWorld().playSound(p, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            this.reloaded.put(uuid, bullet);
            p.getWorld().playSound(p, Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
            p.sendActionBar(ColorUtils.chat(Alert.GREEN + " 장전완료 (탄환 : &b" + bullet.name() + "&f)"));
            replaceSnipe(p);
            this.isReloading.remove(uuid);
            p.setCooldown(tool, 30);
        }, 25L);
    }

    @Override
    public void useSecondSkill(Player p) {
        PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Sniper.getInstance(), emblem, EmblemType.RANGE);
        if (skillUse.isCancelled()) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        UUID uuid = p.getUniqueId();
        if (magicalBullet.contains(uuid)) {
            getPlugin().infoMsg(InfoLevel.ERROR, p, "이미 장전되어 있습니다.");
            return;
        }
        if (isReloading.contains(p.getUniqueId())) {
            getPlugin().infoMsg(InfoLevel.WARN, p, "이미 장전중입니다!");
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        p.sendActionBar(ColorUtils.chat("&a장전중..."));
        this.isReloading.add(uuid);
        p.getWorld().playSound(p, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            BulletType type = BulletType.MAGICAL;
            this.magicalBullet.add(p.getUniqueId());
            p.getWorld().playSound(p, Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
            p.sendActionBar(ColorUtils.chat(Alert.GREEN + " 장전완료 (탄환 : &b" + type.name() + "&f)"));
            replaceSnipe(p);
            this.isReloading.remove(uuid);
            p.setCooldown(tool, 60);
        }, 60L);
    }

    private void replaceSnipe(@NotNull Player p) {
        Inventory inv = p.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null || item.getType().isAir()) continue;
            if (item.getType() != Material.CROSSBOW) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey roleKey = getPlugin().getNamespacedKey("role_id");

            if (!container.has(roleKey, PersistentDataType.STRING)) continue;
            String key = container.get(roleKey, PersistentDataType.STRING);

            if (key != null && key.equals("sniper")) {
                inv.setItem(i, Gun(p));
                return;
            }
        }
    }

    private BulletType selectBullet() {
        Random random = new Random();
        int num = random.nextInt(0, 3);
        return BulletType.getType(num);
    }

    @CanIgnoreReturnValue
    public @Nullable Entity shoot(Player p) {
        UUID uuid = p.getUniqueId();

        if (!this.reloaded.containsKey(uuid) && !this.magicalBullet.contains(uuid)) {
            return null;
        }

        BulletType type = this.reloaded.get(uuid);
        boolean magical = this.magicalBullet.contains(uuid);
        Entity target = null;

        if (magical) {
            this.magicalBullet.remove(uuid);
            type = BulletType.MAGICAL;
            // TODO: magical effect 로직 (범위기, 폭발, 혹은 다른 투사체 등)
            target = fireSniperBullet(p, type.getDistance(), type.getDamage());
            p.playSound(p, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.2f);

            replaceSnipe(p);
            return target;
        }

        if (BulletType.STUN.equals(type)) {
            fireTravelingStunBullet(p, 1.3, type.getDistance(), type.getDamage());
            p.playSound(p, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
        } else {
            target = fireSniperBullet(p, type.getDistance(), type.getDamage());

            if (target != null) {
                lastHitRecord.put(target.getUniqueId(), type);

                p.sendActionBar(ColorUtils.chat("&6&lHIT! &f- " + target.getName()));
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            }
        }

        this.reloaded.remove(uuid);
        replaceSnipe(p);

        return target;
    }

    public void fireTravelingStunBullet(Player p, double speedPerTick, double maxDistance, double damage) {
        Location startLoc = p.getEyeLocation();
        org.bukkit.util.Vector direction = startLoc.getDirection().normalize();
        UUID uuid = p.getUniqueId();
        Particle.DustOptions trailColor = new Particle.DustOptions(Color.YELLOW, 0.8f);

        new BukkitRunnable() {
            private final Location currentLoc = startLoc.clone();
            private double distanceTraveled = 0;

            @Override
            public void run() {
                if (distanceTraveled >= maxDistance) {
                    cancel();
                    return;
                }

                org.bukkit.util.RayTraceResult blockTrace = p.getWorld().rayTraceBlocks(currentLoc, direction, speedPerTick, FluidCollisionMode.NEVER, true);
                org.bukkit.util.RayTraceResult entityTrace = p.getWorld().rayTraceEntities(currentLoc, direction, speedPerTick, 0.2, (entity) ->
                        entity instanceof LivingEntity && !entity.equals(p) && !pm.isParty(uuid, entity.getUniqueId())
                );

                double stepDistance = speedPerTick;
                LivingEntity hitTarget = null;
                boolean hitSomething = false;

                if (blockTrace != null && blockTrace.getHitBlock() != null) {
                    stepDistance = currentLoc.distance(blockTrace.getHitPosition().toLocation(p.getWorld()));
                    hitSomething = true;
                }

                if (entityTrace != null && entityTrace.getHitEntity() instanceof LivingEntity target) {
                    double entityDist = currentLoc.distance(entityTrace.getHitPosition().toLocation(p.getWorld()));
                    if (entityDist < stepDistance) {
                        stepDistance = entityDist;
                        hitTarget = target;
                        hitSomething = true;
                    }
                }

                org.bukkit.util.Vector stepVec = direction.clone().multiply(0.3);
                for (double d = 0; d < stepDistance; d += 0.3) {
                    currentLoc.add(stepVec);
                    p.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, trailColor);
                }

                // 4. 💥 무언가에 부딪혔을 때의 처리
                if (hitSomething) {
                    if (hitTarget != null) {
                        // 🎯 몹이나 플레이어에게 맞았을 때: 스턴 부여 및 데미지
                        sem.StunEntity(hitTarget.getUniqueId(), 40L);
                        lastHitRecord.put(hitTarget.getUniqueId(), BulletType.STUN);

                        hitTarget.damage(damage, p);

                        // 적중 피드백 연출
                        p.sendActionBar(ColorUtils.chat("&e&lSTUN HIT! &f- " + hitTarget.getName()));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
                        hitTarget.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, hitTarget.getLocation().add(0, 1, 0), 1);
                    } else {
                        // 블록(벽)에 맞았을 때 연출
                        p.getWorld().spawnParticle(Particle.BLOCK, currentLoc, 10, 0.1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());
                        p.getWorld().playSound(currentLoc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
                    }

                    cancel();
                    return;
                }

                distanceTraveled += speedPerTick;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    @CanIgnoreReturnValue
    public @Nullable Entity fireSniperBullet(Player p, double maxDistance, double damage) {
        Location startLoc = p.getEyeLocation();
        org.bukkit.util.Vector direction = startLoc.getDirection();
        UUID uuid = p.getUniqueId();

        org.bukkit.util.RayTraceResult blockTrace = p.getWorld().rayTraceBlocks(startLoc, direction, maxDistance, FluidCollisionMode.NEVER, true);

        org.bukkit.util.RayTraceResult entityTrace = p.getWorld().rayTraceEntities(startLoc, direction, maxDistance, 0.2, (entity) ->
                entity instanceof LivingEntity && !entity.equals(p) && !pm.isParty(uuid, entity.getUniqueId())
        );

        double finalDistance = maxDistance;
        LivingEntity hitTarget = null;

        if (blockTrace != null && blockTrace.getHitBlock() != null) {
            finalDistance = startLoc.distance(blockTrace.getHitPosition().toLocation(p.getWorld()));
        }

        if (entityTrace != null && entityTrace.getHitEntity() instanceof LivingEntity target) {
            double entityDist = startLoc.distance(entityTrace.getHitPosition().toLocation(p.getWorld()));
            if (entityDist < finalDistance) {
                finalDistance = entityDist;
                hitTarget = target;
            }
        }

        org.bukkit.util.Vector step = direction.clone().normalize().multiply(0.3);
        Location particleLoc = startLoc.clone();

        BulletType currentType = reloaded.getOrDefault(uuid, BulletType.NORMAL);
        Particle.DustOptions trailColor = getTrailColor(currentType);

        for (double d = 0; d < finalDistance; d += 0.3) {
            particleLoc.add(step);
            p.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, trailColor);
        }

        if (hitTarget != null) {
            hitTarget.damage(damage, p);
            p.getWorld().spawnParticle(Particle.CRIT, hitTarget.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.5);
            p.getWorld().playSound(hitTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

            return hitTarget;
        } else {
            Location missLoc = startLoc.clone().add(direction.clone().multiply(finalDistance));
            p.getWorld().spawnParticle(Particle.BLOCK, missLoc, 5, 0.1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());
        }

        p.getWorld().playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        return null;
    }

    // 🎨 탄환 타입별 트레일 색상 분기 메서드
    private Particle.DustOptions getTrailColor(BulletType type) {
        switch (type) {
            case MAGICAL -> { return new Particle.DustOptions(org.bukkit.Color.PURPLE, 1.1f); }
            case SNEAKY -> { return new Particle.DustOptions(Color.GREEN, 0.6f); }
            default -> { return new Particle.DustOptions(org.bukkit.Color.GRAY, 0.5f); }
        }
    }

    @Override
    public void usePassive(Player p) {

    }

    @Override
    public @NotNull Role getRole() {
        return SecondaryRoles.SNIPER;
    }

    private ItemStack Gun(@Nullable Player p) {
        return ItemBuilder.of(getPlugin(), Material.CROSSBOW)
                .setName(ColorUtils.chat("&4&l인터셉터"))
                .setLore(ColorUtils.chat(p == null || reloaded.get(p.getUniqueId()) == null ? "&7장전되지 않음" : "&6장전됨( " + reloaded.get(p.getUniqueId()).name() + " )"))
                .setTag("role_id", "sniper")
                .setUnbreakable()
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return Gun(null);
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HELMET)
                .setName(ColorUtils.chat("&7&l3뚝"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setTag("role_id", "sniper")
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
