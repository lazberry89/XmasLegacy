package xmasLegacy.SecondaryRoleManager;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode, unused")
public class Defender extends AbstractSecondRole {
	private final @NotNull SkillEffectManager sem;
	private final @NotNull PartyManager pm;

	public Defender() {
		super(SecondaryRoles.DEFENDER);
		this.sem = SkillEffectManager.INSTANCE;
		this.pm = PartyManager.INSTANCE;
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
		if (!consumeEnergy(p, 3)) return;
		Location loc = p.getLocation();

		Location startLoc = loc.add(0, 1.2, 0);
		Vector dir = startLoc.getDirection().normalize();

		Vector axis;
		if (Math.abs(dir.getY()) > 0.9) {
			axis = new Vector(1, 0, 0);
		} else {
			axis = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
		}

		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.5f);

		new BukkitRunnable() {
			double distance = 0;
			final double maxDistance = 10.0;
			final double radius = 0.8;
			final Set<UUID> hitList = new HashSet<>();

			@Override
			public void run() {
				if (distance > maxDistance || !p.isOnline()) {
					this.cancel();
					return;
				}

				for (double step = 0; step < 1.5; step += 0.15) {
					distance += 0.15;
					Location center = startLoc.clone().add(dir.clone().multiply(distance));

					double angle = distance * 2.9; // 회전 속도 (값이 클수록 더 촘촘히 꼬임)

					Vector offset1 = axis.clone().rotateAroundAxis(dir, angle).multiply(radius);
					Vector offset2 = axis.clone().rotateAroundAxis(dir, angle + Math.PI).multiply(radius); // 정반대 편파티클

					p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(offset1), 1, 0, 0, 0, 0);
					p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(offset2), 1, 0, 0, 0, 0);

					center.getNearbyEntities(0.8, 0.8, 0.8).forEach(e -> {
						if (e instanceof LivingEntity target && !target.equals(p)) {
							if (!pm.isParty(p.getUniqueId(), target.getUniqueId())) {
								if (hitList.add(target.getUniqueId())) {
									target.damage(5.0, p);
									sem.StunEntity(target.getUniqueId(), 30L);

									target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
									target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.3f);
								}

							}
						}
					});
				}
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
		p.setCooldown(tool, 20);
	}

	public void spawnShockWave(Location center) {
		new BukkitRunnable() {
			double radius = 0.5;
			final double maxRadius = 5.5;
			final double expansionSpeed = 1.1;

			@Override
			public void run() {
				if (radius > maxRadius) {
					this.cancel();
					return;
				}

				int particleCount = (int) (radius * 40);

				for (int i = 0; i < particleCount; i++) {
					double angle = 2 * Math.PI * i / particleCount;
					double x = Math.cos(angle) * radius;
					double z = Math.sin(angle) * radius;

					Location particleLoc = center.clone().add(x, 0.1, z);

					Particle.DustTransition option = new Particle.DustTransition(Color.RED, Color.BLACK, 1.3f);
					center.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, option);

					if (i % 3 == 0) {
						center.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0.1, 0, 0.05);
					}
				}

				radius += expansionSpeed;
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
		if (!consumeEnergy(p, 3)) return;

		Location loc = p.getLocation();
		SkillEffectManager.startHakiWave(getPlugin(), p.getLocation());
		spawnShockWave(loc.clone().add(0, 0.5, 0));

		p.getNearbyEntities(5, 5, 5).stream()
				.filter(e -> e != p && e instanceof LivingEntity)
				.filter(e -> !pm.isParty(p.getUniqueId(), e.getUniqueId()))
				.map(e -> (LivingEntity) e)
				.forEach(le -> {
					le.damage(8.0, p);

					Vector pushVelocity = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5);
					pushVelocity.setY(0.3);
					le.setVelocity(pushVelocity);
				});
		p.getWorld().playSound(p, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 1.3f);
		p.setCooldown(tool, 20);
	}

	@Override
	public void usePassive(@NotNull Player p) {}

	@Override
	public @NotNull Role getRole() {
		return SecondaryRoles.DEFENDER;
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
		return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
				.setName(ColorUtils.chat("&7&l단단한 갑옷"))
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
