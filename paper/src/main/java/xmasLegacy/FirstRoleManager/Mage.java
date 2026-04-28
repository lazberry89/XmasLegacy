package xmasLegacy.FirstRoleManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.lazberry.xmaslegacy.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.*;

public class Mage extends AbstractFirstRole {
    private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
    public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.COMPACT_INSANELY);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
    private final SkillEffectManager SEM;

	public Mage(int c1, int c2, XmasLegacy plugin, SkillEffectManager SEM) {
		super(c1, c2, plugin);
        this.SEM = SEM;
	}

	@Override
	public void useFirstSkill(Player p) {
        ItemStack tool = p.getInventory().getItemInMainHand();

        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(p, 6)) return;
        Location startLoc = p.getEyeLocation();
        Vector dir = startLoc.getDirection().normalize().multiply(0.4); // 매우 느린 속도

        // 응축체 비주얼용 아머스탠드 (보이지 않음)
        ArmorStand orb = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
        });

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 60 || !orb.isValid() || orb.getLocation().getBlock().isSolid()) { // 3초 후 자동 소멸
                    explode(orb.getLocation(), p);
                    orb.remove();
                    this.cancel();
                    return;
                }

                orb.teleport(orb.getLocation().add(dir));
                // 보라색 응축체 파티클
	            Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
                orb.getWorld().spawnParticle(Particle.DUST, orb.getLocation(), 15, 0.1, 0.1, 0.1, 0.02, dust);

                // 충돌 검사
                for (Entity e : orb.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (e instanceof LivingEntity && !e.equals(p)) {
                        explode(orb.getLocation(), p);
                        orb.remove();
                        this.cancel();
                        return;
                    }
                }
                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0, 1);


        p.setCooldown(tool, getCooldown1() * 20);
	}
    private void explode(Location loc, Player source) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
        loc.getWorld().createExplosion(source, loc, 4, false, false);
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.3, 2, 1.3)) {
            if (e instanceof LivingEntity le && !e.equals(source)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1, true, false, false));
            }
        }
    }

	@SuppressWarnings("DuplicatedCode")
	@Override
	public void useSecondSkill(Player p) {
        ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }

        if (!consumeEnergy(p, 8)) return;

        final Location center = p.getEyeLocation().add(p.getLocation().getDirection().multiply(8));

        if (center.getBlock().getType().isSolid()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 해당 위치에 스킬을 사용할 수 없습니다!"));
            return;
        }
        List<BlockDisplay> cores = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BlockDisplay bd = center.getWorld().spawn(center.clone().subtract(0.5, 0.5, 0.5), BlockDisplay.class, display -> {
                display.setBlock(Material.PURPLE_STAINED_GLASS.createBlockData());
                display.setBrightness(new Display.Brightness(15, 15)); // 발광 효과

                // 초기 크기 및 변환 설정
                Transformation trans = display.getTransformation();
                trans.getScale().set(1.2f, 1.2f, 1.2f); // 살짝 크게
                display.setTransformation(trans);
                display.setInterpolationDuration(1); // 부드러운 애니메이션을 위한 보간 설정
                display.setInterpolationDelay(0);
            });
            cores.add(bd);
        }

		Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
        SEM.drawCircularLine(center, Particle.DUST, 3, true, 120, dust);
        SEM.drawCircularLine(center.clone().add(0, -0.5, 0), Particle.DUST, 2.5, true, 120, dust);
        SEM.drawCircularLine(center.clone().add(0, 0.5, 0), Particle.DUST, 2.5, true, 120, dust);

        p.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.7f);

        // 쿨타임 설정
        p.setCooldown(tool, getCooldown2() * 20);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 50 || !p.isOnline()) {
                    cores.forEach(Entity::remove);
                    this.cancel();
                    return;
                }

                for (int i = 0; i < cores.size(); i++) {
                    BlockDisplay bd = cores.get(i);
                    Transformation trans = bd.getTransformation();

                    float angle = (float) Math.toRadians(ticks * 15); // 프레임당 회전 속도
                    if (i == 0) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle, angle * 0.5f, 0));
                    else if (i == 1) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(0, angle, angle * 0.5f));
                    else trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle * 0.5f, 0, angle));

                    bd.setTransformation(trans);
                    bd.setInterpolationDuration(1);
                }

                center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 10, 0.5, 0.5, 0.5, 0.1);
                for (Entity e : center.getWorld().getNearbyEntities(center, 6.0, 6.0, 6.0)) {

                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        Vector direction = center.toVector().subtract(le.getLocation().toVector());

                        double distance = direction.length();

                        if (distance > 0.6) {
                            direction.normalize();
                            double pullStrength = 0.35;
                            le.setVelocity(direction.multiply(pullStrength).setY(0.1));
                        } else {
                            le.setVelocity(new Vector(0, 0.02, 0));
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0, 1);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MAGE;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.BREEZE_ROD)
                .setName(ColorUtils.chat("&7&l일반 지팡이"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setAttackDamage(4)
                .setTag("role_id", "mage")
                .hideAllFlags()
                .addAttribute(Attribute.MOVEMENT_SPEED, -0.08, AttributeModifier.Operation.ADD_NUMBER)
                .setGlint(true)
                .build().clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.DIAMOND_CHESTPLATE)
                .setName(ColorUtils.chat("&7&l보호구"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .hideAllFlags()
                .addAttribute(Attribute.JUMP_STRENGTH, 0.04, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
                .setArmorState(7, EquipmentSlotGroup.CHEST)
                .setTag("role_id", "MageArmor")
                .build().clone();
    }
}
