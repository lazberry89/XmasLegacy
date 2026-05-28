package xmasLegacy.SecondaryRoleManager;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

@SuppressWarnings("DuplicatedCode, unused")
public class Defender extends AbstractSecondRole {
	private final SkillEffectManager SEM;
	private final PartyManager PM;

	private static Defender instance;

	public static Defender getInstance() {
		if (instance == null) instance = new Defender();
		return instance;
	}

	private Defender() {
		super(SecondaryRoles.DEFENDER);
		this.SEM = SkillEffectManager.getInstance();
		this.PM = PartyManager.getInstance();
	}

	@Override
	public void useFirstSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Defender.getInstance(), emblem, EmblemType.TARGET);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		Location loc = p.getLocation();
		p.getNearbyEntities(5, 5, 5).forEach(e -> {
			if (e instanceof LivingEntity target && !target.equals(p)) {
				if (!PM.isParty(p.getUniqueId(), target.getUniqueId())) {
					Location targetLoc = target.getLocation();
					Vector direction = loc.toVector().subtract(targetLoc.toVector());

					direction.setY(0.5);

					Vector velocity = direction.normalize().multiply(2);
					target.setVelocity(velocity);
					SEM.drawLine(loc, targetLoc, Particle.SOUL_FIRE_FLAME, 0.1, false, false, 3, p);
					p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.3f);
				}
			}
		});
		p.setCooldown(tool, 20);
	}

	public void spawnShockWave(Location center) {
		new BukkitRunnable() {
			int ticks = 0;
			final int duration = 8;
			final double maxRadius = 5.0;

			@Override
			public void run() {
				if (ticks >= duration) {
					this.cancel();
					return;
				}

				double radius = maxRadius * ((double) ticks / duration);
				double circumference = 2 * Math.PI * radius;
				int points = (int) (circumference / 0.2);
				if (points < 1) points = 1;

				for (int i = 0; i < points; i++) {
					double angle = (2 * Math.PI / points) * i;
					double x = Math.cos(angle) * radius;
					double z = Math.sin(angle) * radius;

					center.getWorld().spawnParticle(
							Particle.SWEEP_ATTACK,
							center.clone().add(x, 0.1, z),
							1, 0, 0, 0, 0
					);
					center.getWorld().spawnParticle(
							Particle.SOUL_FIRE_FLAME,
							center.clone().add(x, 0.1, z),
							1, 0, 0, 0, 0
					);
				}
				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
	}

	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, Defender.getInstance(), emblem, EmblemType.RANGE);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		Location loc = p.getLocation();
		//spawnShockWave(loc);
		SkillEffectManager.startHakiWave(getPlugin(), p.getLocation());
		p.getNearbyEntities(5, 5, 5).stream()
				.filter(e -> e != p && e instanceof LivingEntity)
				.filter(e -> !PM.isParty(p.getUniqueId(), e.getUniqueId()))
				.map(e -> (LivingEntity) e)
				.forEach(le -> {
					le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20, 30));
					SkillEffectManager.getInstance().StunEntity(le.getUniqueId(), 20);
					le.setPose(Pose.SLEEPING, true);
					le.damage(6, p);
					Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
						if (le.isValid() && le.getPose().equals(Pose.SLEEPING)) le.setPose(Pose.STANDING, true);
					}, 20L);
				});
		p.getWorld().playSound(p, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 1.3f);
		p.setCooldown(tool, 20);
	}

	@Override
	public void usePassive(Player p) {}

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
