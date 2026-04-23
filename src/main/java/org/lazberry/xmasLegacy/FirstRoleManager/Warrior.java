package org.lazberry.xmasLegacy.FirstRoleManager;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Skill.BasicSkills;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.GlowUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Warrior extends AbstractFirstRole {
    private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
    public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.BLOOD_FRENZY);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Warrior(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool)/20 + "&f초 기다리세요"));
			return;
		}
        if (!consumeEnergy(p, 3)) return;
		p.damage(8);
		p.getWorld().strikeLightningEffect(p.getLocation());
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

		for (Entity entity : p.getNearbyEntities(5, 5, 5)) {
			if (entity instanceof LivingEntity le && !le.equals(p)) {
				le.damage(4, p);
				le.teleport(le.getLocation().add(0, 0.1, 0));
				le.setVelocity(le.getVelocity().add(new Vector(0, 0.7, 0)));
			}
		}

		p.setCooldown(tool, getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, 3)) return;
		Location startLoc = p.getEyeLocation();
		// 1. 벡터 복사본 생성 및 속도 설정 (원본 보존을 위해 clone 사용)
		final Vector direction = startLoc.getDirection().clone().normalize().multiply(1.0);
		final float playerYaw = p.getLocation().getYaw();

		ArmorStand axeStand = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setArms(true);
			stand.setBasePlate(false);
			stand.setMarker(true);
			GlowUtils.setGlowColor(stand, NamedTextColor.RED);

			Location loc = stand.getLocation();
			loc.setYaw(playerYaw);
			stand.teleport(loc);

			stand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
		});

		p.setCooldown(tool, this.getCooldown2() * 20);

		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = 40;

			@Override
			public void run() {
				// 종료 조건: 시간 초과 또는 아머스탠드 소멸
				if (ticks >= maxTicks || !axeStand.isValid()) {
					if (axeStand.isValid()) axeStand.remove();
					this.cancel();
					return;
				}

				Location currentLoc = axeStand.getLocation().add(direction);
				axeStand.teleport(currentLoc);
				axeStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 3, 0.05, 0.05, 0.05, 0.01);

				double rotation = ticks * 0.6;
				axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));

				for (Entity entity : axeStand.getNearbyEntities(1.2, 1.2, 1.2)) {
					if (entity instanceof LivingEntity target && !entity.equals(p)) {

						Location targetLoc = target.getLocation();
						Vector targetDir = targetLoc.getDirection().normalize();
						Location backLoc = targetLoc.clone().subtract(targetDir.multiply(1.5));

						if (backLoc.getBlock().getType().isSolid()) {
							backLoc.add(0, 1.0, 0);
						}

						p.teleport(backLoc);
						p.playSound(backLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
						target.damage(6.0, p);

						axeStand.remove();
						this.cancel();
						return;
					}
				}

				// 벽 충돌 시 제거
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
	public Roles getRole() {
		return Roles.WARRIOR;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_AXE)
				.setName(ColorUtils.chat("&8&l무거운 도끼"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "warrior")
				.build().clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
		        .setName(ColorUtils.chat("&8&l전사의 갑옷"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setUnbreakable()
		        .hideAllFlags()
		        .setArmorState(9, EquipmentSlotGroup.CHEST)
                .addAttribute(Attribute.SCALE, 0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
                .addAttribute(Attribute.MAX_HEALTH, 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
		        .setTag("role_id", "WarriorArmor")
		        .build().clone();
    }
}
