package org.lazberry.xmaslegacy.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class FireworkCreator {
	private int power = 1;
	private final List<FireworkEffect> effects = new ArrayList<>();
	private FireworkEffect.Type currentType = FireworkEffect.Type.BALL;
	private boolean currentFlicker = false;
	private boolean currentTrail = false;
	private final List<Color> currentColors = new ArrayList<>(List.of(Color.WHITE));
	private final List<Color> currentFades = new ArrayList<>();

	public static FireworkCreator builder() {
		return new FireworkCreator();
	}

	public FireworkCreator power(int power) {
		this.power = power;
		return this;
	}

	public FireworkCreator setType(FireworkEffect.Type type) {
		this.currentType = type;
		return this;
	}

	public FireworkCreator setFlicker(boolean flicker) {
		this.currentFlicker = flicker;
		return this;
	}

	public FireworkCreator setTrail(boolean trail) {
		this.currentTrail = trail;
		return this;
	}

	public FireworkCreator addColor(Color... colors) {
		this.currentColors.addAll(List.of(colors));
		return this;
	}

	public FireworkCreator addFade(Color... colors) {
		this.currentFades.addAll(List.of(colors));
		return this;
	}

	public FireworkCreator addEffect() {
		if (!currentColors.isEmpty()) {
			effects.add(FireworkEffect.builder()
					.with(currentType)
					.flicker(currentFlicker)
					.trail(currentTrail)
					.withColor(currentColors)
					.withFade(currentFades)
					.build());
			resetCurrentEffect();
		}
		return this;
	}

	private void resetCurrentEffect() {
		this.currentType = FireworkEffect.Type.BALL;
		this.currentFlicker = false;
		this.currentTrail = false;
		this.currentColors.clear();
		this.currentFades.clear();
	}

	private void commitPendingEffect() {
		if (!currentColors.isEmpty()) {
			addEffect();
		}
	}

	public ItemStack buildItem() {
		commitPendingEffect();
		ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		if (meta != null) {
			meta.setPower(power);
			meta.addEffects(effects);
			item.setItemMeta(meta);
		}
		return item;
	}

	public Firework spawn(Location loc) {
		if (loc == null || loc.getWorld() == null) return null;
		commitPendingEffect();
		return loc.getWorld().spawn(loc, Firework.class, fw -> {
			FireworkMeta meta = fw.getFireworkMeta();
			meta.setPower(power);
			meta.addEffects(effects);
			fw.setFireworkMeta(meta);
		});
	}

	public FireworkCreator addEffect(FireworkEffect.Type type, boolean flicker, boolean trail, Color... colors) {
		if (colors.length > 0) {
			effects.add(FireworkEffect.builder()
					.with(type)
					.flicker(flicker)
					.trail(trail)
					.withColor(colors)
					.build());
		}
		return this;
	}

	public FireworkCreator addEffect(FireworkEffect.Type type, boolean flicker, boolean trail, List<Color> colors, List<Color> fades) {
		FireworkEffect.Builder b = FireworkEffect.builder().with(type).flicker(flicker).trail(trail);
		if (colors != null && !colors.isEmpty()) b.withColor(colors);
		if (fades != null && !fades.isEmpty()) b.withFade(fades);
		effects.add(b.build());
		return this;
	}
}
