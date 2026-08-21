package org.lazberry.xmaslegacy.cosmetics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cosmetics {
	@EqualsAndHashCode.Include
	private final @NotNull @Getter ItemStack model;
	private final @NotNull @Getter String name;
	private final @NotNull CosmeticType type;
	private final @NotNull Map<UUID, ItemDisplay> display = new HashMap<>();
	private final @NotNull Map<UUID, BukkitTask> task = new HashMap<>();
	private final @NotNull XmasLegacy plugin;

	public Cosmetics(@NotNull ItemStack model, @NotNull String name, @NotNull CosmeticType type) {
		this.model = model;
		this.name = name;
		this.type = type;
		this.plugin = XmasLegacy.getInstance();
	}

	private void spawnCosmeticDisplay(@NotNull Player p) {
		ItemDisplay display = p.getWorld().spawn(p.getLocation(), ItemDisplay.class);
		display.setItemStack(model);
		display.setInvisible(true);
		display.setInvulnerable(true);
		display.setGravity(false);

		display.setBillboard(Display.Billboard.VERTICAL);

		org.bukkit.util.Transformation transformation = display.getTransformation();

		if (type == CosmeticType.BODY) {
			transformation.getTranslation().set(0.0f, -0.65f, -0.75f);
			transformation.getScale().set(1.3f, 1.3f, 1.3f);
		} else if (type == CosmeticType.HEAD) {
			transformation.getTranslation().set(0.0f, 0.2f, 0.0f);
			transformation.getScale().set(1.0f, 1.0f, 1.0f);
		}

		display.setTransformation(transformation);
		display.setInterpolationDuration(0);

		p.addPassenger(display);

		this.display.put(p.getUniqueId(), display);
	}

	private void updateCosmeticDisplay(@NotNull Player p, @NotNull ItemDisplay display) {
		float pitch = p.getLocation().getPitch();
		if (type == CosmeticType.BODY) {
			if (pitch > 60 && display.isVisibleByDefault()) {
				display.setVisibleByDefault(false);
			} else if (pitch <= 60 && !display.isVisibleByDefault()) {
				display.setVisibleByDefault(true);
			}
		}
	}

	private void startUpdateLoop(@NotNull Player p) {
		UUID uuid = p.getUniqueId();

		BukkitTask newTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
			ItemDisplay d = this.display.get(uuid);

			if (!p.isOnline() || d == null || d.isDead() || !p.getPassengers().contains(d)) {
				unequip(p);
				return;
			}

			updateCosmeticDisplay(p, d);

		}, 0L, 1L);
		this.task.put(uuid, newTask);
	}

	public void equip(Player p) {
		unequip(p);
		spawnCosmeticDisplay(p);
		startUpdateLoop(p);
	}

	public void unequip(Player p) {
		UUID uuid = p.getUniqueId();
		ItemDisplay display = this.display.get(uuid);
		BukkitTask task = this.task.get(uuid);

		if (display != null) {
			display.remove();
			this.display.remove(uuid);
		}
		if (task != null) {
			task.cancel();
			this.task.remove(uuid);
		}
	}
}