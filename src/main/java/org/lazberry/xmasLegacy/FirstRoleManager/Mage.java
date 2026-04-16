package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Skill.BasicSkills;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        if (!consumeEnergy(p, 6)) return;

        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
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

	@Override
	public void useSecondSkill(Player p) {
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
            return;
        }

        if (!consumeEnergy(p, 8)) return;

        final Location center = p.getEyeLocation().add(p.getLocation().getDirection().multiply(8));

        if (center.getBlock().getType().isSolid()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 해당 위치에 스킬을 사용할 수 없습니다!"));
            return;
        }

        // 마법진 연출 (center를 직접 add하지 않고 clone()으로 파생 좌표 생성)
		Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
        SEM.drawCircularLine(center, Particle.DUST, 3, true, 120, dust);
        SEM.drawCircularLine(center.clone().add(0, -0.5, 0), Particle.DUST, 2.5, true, 120, dust);
        SEM.drawCircularLine(center.clone().add(0, 0.5, 0), Particle.DUST, 2.5, true, 120, dust);

        p.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.7f);

        // 쿨타임 설정
        p.setCooldown(tool.getType(), getCooldown2() * 20);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 50 || !p.isOnline()) {
                    this.cancel();
                    return;
                }

                // 블랙홀 시각 효과 강화
                center.getWorld().spawnParticle(Particle.SQUID_INK, center, 3, 0.3, 0.3, 0.3, 0.05);
                center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 7, 0.2, 0.2, 0.2, 0.1);

                for (Entity e : center.getWorld().getNearbyEntities(center, 6.0, 6.0, 6.0)) {
                    if (e instanceof LivingEntity le && !e.equals(p)) {
                        Vector direction = center.toVector().subtract(le.getLocation().toVector());
                        double distance = direction.length();

                        if (distance > 0.6) { // 0.5보다 살짝 늘려 떨림 방지
                            direction.normalize();
                            double pullStrength = 0.35;
                            // Y축을 0.1로 살짝 높여 마찰력을 더 확실히 제거
                            le.setVelocity(direction.multiply(pullStrength).setY(0.1));
                        } else {
                            // 중심부에 도달하면 멈춰있게 만듦 (빨려들어온 후 고정 효과)
                            le.setVelocity(new Vector(0, 0.02, 0));
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0, 1);
	}

	@Override
	public Roles getRole() {
		return Roles.MAGE;
	}

	@Override
	public ItemStack roleWeapon() {
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
    public ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.DIAMOND_CHESTPLATE)
                .setName(ColorUtils.chat("&7&l보호구"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .hideAllFlags()
                .addAttribute(Attribute.JUMP_STRENGTH, 0.04, AttributeModifier.Operation.ADD_NUMBER)
                .setTag("role_id", "MageArmor")
                .build().clone();
    }
}
