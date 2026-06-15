package xmaslegacy.Cosmetics;

import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cosmetics {
	private final ItemStack model;
	private final String name;
	private final CosmeticType type;
	private final Map<UUID, ItemDisplay> display = new HashMap<>();
	private final Map<UUID, BukkitTask> task = new HashMap<>();
	private final XmasLegacy plugin = JavaPlugin.getPlugin(XmasLegacy.class);

	public Cosmetics(ItemStack model, String name, CosmeticType type) {
		this.model = model;
		this.name = name;
		this.type = type;
	}

	public ItemStack getModel() {return model;}
	public String getName() {return name;}

	private void spawnCosmeticDisplay(Player p) {
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

	private void updateCosmeticDisplay(Player p, ItemDisplay display) {
		// 1인칭 시야 가림 방지 (Pitch 값에 따라 숨김)
		float pitch = p.getLocation().getPitch();
		if (type == CosmeticType.BODY) {
			if (pitch > 60 && display.isVisibleByDefault()) {
				display.setVisibleByDefault(false);
			} else if (pitch <= 60 && !display.isVisibleByDefault()) {
				display.setVisibleByDefault(true);
			}
		}
	}

	private void startUpdateLoop(Player p) {
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