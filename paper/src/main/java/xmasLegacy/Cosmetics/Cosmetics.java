package xmasLegacy.Cosmetics;

import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cosmetics {
	private final ItemStack model;
	private final String name;
	private final Map<UUID, ItemDisplay> display = new HashMap<>();
	private final Map<UUID, BukkitTask> task = new HashMap<>();
	private final Map<UUID, Float> lastYaw = new HashMap<>();
	private final XmasLegacy plugin = JavaPlugin.getPlugin(XmasLegacy.class);

	public Cosmetics(ItemStack model, String name) {
		this.model = model;
		this.name = name;
	}

	public ItemStack getModel() {return model;}
	public String getName() {return name;}

	private void spawnCosmeticDisplay(Player p) {
		ItemDisplay display = p.getWorld().spawn(p.getLocation(), ItemDisplay.class);
		display.setItemStack(model);
		display.setInvisible(true);
		display.setInvulnerable(true);
		display.setGravity(false);

		// Billboard를 FIXED로 설정 (고정된 각도 유지)
		display.setBillboard(Display.Billboard.FIXED);

		org.bukkit.util.Transformation transformation = display.getTransformation();
		transformation.getTranslation().set(0.0f, -0.7f, -1.0f);
		transformation.getScale().set(1.3f, 1.3f, 1.3f);
		display.setTransformation(transformation);
		display.setInterpolationDuration(3);

		// 플레이어에게 passenger로 태움
		p.addPassenger(display);

		this.display.put(p.getUniqueId(), display);
	}

	private void updateCosmeticDisplay(Player p, ItemDisplay display) {
		UUID uuid = p.getUniqueId();
		float bodyYaw = p.getYaw();
		float pitch = p.getLocation().getPitch();

		if (Math.abs(bodyYaw - lastYaw.getOrDefault(uuid, bodyYaw)) > 1.0f) {
			display.setRotation(bodyYaw, 0);
			lastYaw.put(uuid, bodyYaw);
		}

		if (pitch > 60 && display.isVisibleByDefault()) {
			display.setVisibleByDefault(false);
		} else if (pitch <= 60 && !display.isVisibleByDefault()) {
			display.setVisibleByDefault(true);
		}
	}

	private void startUpdateLoop(Player p) {
		UUID uuid = p.getUniqueId();
		ItemDisplay display = this.display.get(uuid);

		if (display == null) return;

		BukkitTask newTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
			ItemDisplay d = this.display.get(uuid);

			if (!p.isOnline() || d == null || d.isDead()) {
				BukkitTask t = task.remove(uuid);
				if (t != null) t.cancel();

				if (d != null) d.remove();
				Cosmetics.this.display.remove(uuid);
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
		lastYaw.remove(uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Cosmetics c)) return false;
		return c.getModel().equals(model);
	}

	@Override
	public int hashCode() {
		return model.hashCode();
	}
}
