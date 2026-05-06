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


		// Transformation 설정: 위치 조정 및 크기 확대
		// Y: -0.7f (내려옴), Z: -1.0f (더 뒤로)
		// Scale: 1.3f (30% 더 큼)
		org.bukkit.util.Transformation transformation = display.getTransformation();
		transformation.getTranslation().set(0.0f, -0.7f, -1.0f);
		transformation.getScale().set(1.3f, 1.3f, 1.3f);
		display.setTransformation(transformation);
		display.setInterpolationDuration(5);

		// 플레이어에게 passenger로 태움
		p.addPassenger(display);

		this.display.put(p.getUniqueId(), display);
	}

	private void updateCosmeticDisplay(Player p, ItemDisplay display) {
		org.bukkit.Location loc = p.getLocation();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();

		// 회전만 업데이트 (passenger로 태웠으므로 위치는 자동 동기화)
		display.setRotation(yaw, 0);

		// 플레이어가 너무 아래를 볼 때 코스메틱 숨김
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

		}, 0L, 1L);  // 매 틱마다 업데이트 (부드러운 동기화)
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
