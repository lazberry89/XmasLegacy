package org.lazberry.xmaslegacy.enchant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantifyEffectManager implements Initiator {
	private final EnchantManager enchantManager;
	private final EnchantConfig config;
	private final XmasLegacy plugin;
	private @Getter Location anvilLoc;
	private BukkitTask task;

	@Inject
	public EnchantifyEffectManager(EnchantManager enchantManager, EnchantConfig config, XmasLegacy plugin) {
		this.enchantManager = enchantManager;
		this.config = config;
		this.plugin = plugin;
	}

	@Override
	public void init() {
		anvilLoc = config.loadSync();
	}

	@Override
	public void close() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		config.saveSync(anvilLoc);
	}

	public void setAnvilLocation(Location loc) {
		this.anvilLoc = loc;
	}

	public boolean startEnchant(Player p) {
		return playEnchantifyEffect(p, anvilLoc.clone().add(0.5, 1, 0.5));
	}

	private ItemDisplay createEffectDisplay(ItemStack item, Location loc) {
		return loc.getWorld().spawn(loc.clone().add(0, 1.3, 0), ItemDisplay.class, d -> {
			GlowUtils.glow(d, NamedTextColor.AQUA);
			d.setItemStack(item);
			Transformation transformation = new Transformation(
					new Vector3f(0, 0, 0),
					new AxisAngle4f((float) Math.toRadians(-45), 0f, 0f, 1f),
					new Vector3f(1.2f, 1.2f, 1.2f),
					new AxisAngle4f(0, 0, 0, 1)
			);
			d.setTransformation(transformation);
			d.setBrightness(new Display.Brightness(15, 5));
			d.setInterpolationDuration(1);
			d.setInterpolationDelay(0);
		});
	}

	private void playEndEffect(Location loc) {
		if (loc == null) return;
		World world = loc.getWorld();
		world.spawnParticle(
				Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, loc, 30, 0.15, 0.15, 0.15, 0.01);
		world.spawnParticle(Particle.END_ROD, loc, 20, 0.3, 0.3, 0.3, 0.25);
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.0f);
		world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_FALL, 2f, 0.3f);
	}

	public boolean playEnchantifyEffect(Player player, Location loc) {
		ItemStack item = player.getInventory().getItemInMainHand();
		Material type = item.getType();

		if (type.isAir()) return false;
		if (!enchantManager.isEnchantableMaterial(type)) {
			InfoUtils.error(player, "강화 가능한 아이템이 아닙니다.");
			return false;
		}
		if (enchantManager.isEnchantable(item)) {
			InfoUtils.error(player, "이미 강화를 시작한 아이템입니다.");
			return false;
		}
		if (loc == null) {
			InfoUtils.error(player, "강화모루의 위치가 정해지지 않았어요!");
			log.error("Enchantify Effect Location is Missing!");
			return false;
		}
		if (task != null) {
			InfoUtils.error(player, "이미 누군가가 사용하고 있어요.");
			return false;
		}

		World world = loc.getWorld();
		ItemStack cloned = item.clone();
		cloned.setAmount(1);

		var display = createEffectDisplay(cloned, loc);
		item.setAmount(item.getAmount() - 1);

		world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
		task = new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = 100;

			float yaw = 0f;
			final float minSpeed = 2f;
			final float maxSpeed = 50f;

			@Override
			public void run() {
				if (display.isDead() || ticks >= maxTicks) {
					if (ticks >= maxTicks) {
						boolean result = enchantManager.setEnchantable(cloned);
						if (result) {
							playEndEffect(loc);
							InventoryHelper.giveItemOrDrop(player, cloned);
						} else {
							InfoUtils.error(player, "해당 무기에 강화를 시작할 수 없어요!");
							world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
							InventoryHelper.giveItemOrDrop(player, cloned);
						}
						display.remove();
					} else {
						InventoryHelper.giveItemOrDrop(player, cloned);
					}

					task = null;
					this.cancel();
					return;
				}

				float progress = (float) ticks / maxTicks;
				float currentSpeed = minSpeed + (maxSpeed - minSpeed) * (progress * progress);

				yaw = (yaw + currentSpeed) % 360f;
				display.setRotation(yaw, 0f);

				ticks++;
			}
		}.runTaskTimer(plugin, 0L, 1L);
		return true;
	}
}
