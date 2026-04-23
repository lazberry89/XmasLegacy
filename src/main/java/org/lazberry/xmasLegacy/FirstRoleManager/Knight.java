package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Skill.BasicSkills;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.*;

public class Knight extends AbstractFirstRole {
    private float Damage = 5;
    private final SkillEffectManager SEM;
    private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
    public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.SHARP_SWEEPING);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Knight(SkillEffectManager SEM, XmasLegacy plugin) {
		super(4, 4, plugin);
        this.SEM = SEM;
	}

	@Override
	public void useFirstSkill(Player player) { //Sharp Sweeping
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (player.getCooldown(tool) > 0) {
            player.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) player.getCooldown(tool)/20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(player, 3)) return;
        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(1.5).setY(0.2));

        player.setPose(Pose.SPIN_ATTACK);

        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 1);
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 10; // 0.5초 동안 지속 (20틱 = 1초)
            final Set<UUID> hitEntities = new HashSet<>();

            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    player.setPose(Pose.STANDING);
                    this.cancel();
                    return;
                }
                player.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 1);
                player.setPose(Pose.SPIN_ATTACK);
                Vector currentV = player.getVelocity();
                player.setVelocity(currentV.add(new Vector(0, 0.05, 0)));

                for (Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity target && !entity.equals(player)) {

                        if (hitEntities.contains(target.getUniqueId())) continue;

                        target.damage(5.0, player); // 5 대미지 (하트 2.5칸)
                        target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2, false, false, true));

                        Vector push = direction.clone().multiply(0.5).setY(0.2);
                        target.setVelocity(push);

                        hitEntities.add(target.getUniqueId());
                    }
                }

                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L); // 매 틱(1/20초)마다 실행
        player.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) { //Taunt
        ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool)/20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(p, 3)) return;
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 30, 10, 10, 10, 0.01);
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.6f);
        for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), 10.0, 10.0, 10.0)) {
            if (entity instanceof LivingEntity e && !p.equals(e)) {
                if (e instanceof Mob mob) {
					mob.setTarget(p);
					mob.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, mob.getLocation(), 10, 1.5, 1.5, 1.5, 0.1);
	                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
		                if (mob.isValid()) {
							mob.setAI(false);
							mob.setMemory(MemoryKey.ANGRY_AT, null);
			                mob.setMemory(MemoryKey.UNIVERSAL_ANGER, null);
							mob.setMemory(MemoryKey.GOLEM_DETECTED_RECENTLY, null);
							mob.setMemory(MemoryKey.HUNTED_RECENTLY, null);
							mob.setMemory(MemoryKey.DANGER_DETECTED_RECENTLY, null);
							mob.setTarget(null);
							Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
								mob.setAI(true);
							}, 3L);
		                }
					}, 100L);
                }
				if (e instanceof Player target) {
					SEM.knockbackEntity(p, target, -1.5, 0.15);
				}

            }
        }
        p.setCooldown(tool, this.getCooldown2() * 20);
	}
	@Override
	public Roles getRole() {
		return Roles.KNIGHT;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_SWORD)
                .setName(ColorUtils.chat("&7&l녹슨 철검"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .hideAllFlags()
                .setItemModel("BasicSword")
                .setAttackDamage(this.Damage)
                .setTag("role_id", "knight")
                .build()
                .clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
                .setName(ColorUtils.chat("&7&l낡은 흉갑"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .hideAllFlags()
                .setItemModel("KnightArmor")
                .setTag("role_id", "KnightArmor")
		        .addAttribute(Attribute.ARMOR, 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
                .build()
                .clone();
    }
}
