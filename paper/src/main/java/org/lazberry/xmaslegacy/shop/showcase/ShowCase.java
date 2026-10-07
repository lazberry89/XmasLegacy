package org.lazberry.xmaslegacy.shop.showcase;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.shop.goods.Goods;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

import java.util.UUID;

@Data
public class ShowCase {
	public static final NamespacedKey key = KeyUtils.get("showcase");
	private final BlockData data = Material.GLASS.createBlockData();
	private final Goods goods;
	private Location baseLocation;
	private BlockFace face;
	private UUID spawnedItem;
	private UUID spawnedInteraction;
	private UUID spawnedDisplay;
	private UUID glass;

	public ShowCase(Goods goods) {
		this.goods = goods;
	}

	public ShowCase(Goods goods, Location baseLocation, BlockFace face) {
		this.goods = goods;
		this.baseLocation = baseLocation;
		this.face = face;
	}

	public void cleanUp() {
		if (this.spawnedItem != null) {
			OptionalUtils.ifNotNull(Bukkit.getEntity(this.spawnedItem), Entity::remove);
			this.spawnedItem = null;
		}
		if (this.spawnedInteraction != null) {
			OptionalUtils.ifNotNull(Bukkit.getEntity(this.spawnedInteraction), Entity::remove);
			this.spawnedInteraction = null;
		}
		if (this.spawnedDisplay != null) {
			OptionalUtils.ifNotNull(Bukkit.getEntity(this.spawnedDisplay), Entity::remove);
			this.spawnedDisplay = null;
		}
		if (this.glass != null) {
			OptionalUtils.ifNotNull(Bukkit.getEntity(this.glass), Entity::remove);
			this.glass = null;
		}
	}

	public Item spawnShowcase(Location loc) {
		Component price = ColorUtils.chat(goods.getPrice() + "&6$");
		Location spawnLoc = loc.clone().add(0.5, 1.1, 0.5);

		loc.getWorld().spawn(loc.clone().add(0, 1, 0), BlockDisplay.class, b -> {
			b.setBlock(data);
			Transformation transformation = new Transformation(
					new org.joml.Vector3f(0, 0, 0),
					new org.joml.AxisAngle4f(0, 0, 0, 1),
					new org.joml.Vector3f(1.0f, 1.5f, 1.0f),
					new org.joml.AxisAngle4f(0, 0, 0, 1)
			);
			b.setTransformation(transformation);
			KeyUtils.set(b, key, goods.getName());
			glass = b.getUniqueId();
		});

		return spawnLoc.getWorld().spawn(spawnLoc, Item.class, i -> {
			i.setCanPlayerPickup(false);
			i.setCanMobPickup(false);
			i.setPersistent(false);
			i.setItemStack(goods.getItem());
			i.setUnlimitedLifetime(true);
			i.setWillAge(false);
			i.setInvulnerable(true);
			i.setGravity(false);
			i.setVelocity(new Vector(0, 0, 0));
			KeyUtils.set(i, key, goods.getName());
			Block block = spawnLoc.getBlock();
			block.setType(Material.LIGHT);

			if (block.getBlockData() instanceof Light light) {
				light.setLevel(13);
				block.setBlockData(light);
			}
			//GlowUtils.glow(i, goods.getColorByRate());
			spawnedItem = i.getUniqueId();
		});
	}

	public Interaction spawnWallButton(Location blockLoc, BlockFace face) {
		this.baseLocation = blockLoc;
		this.face = face;

		Location textLoc = calculateFaceLocation(blockLoc.getBlock().getLocation(), face);

		textLoc.getWorld().spawn(textLoc, TextDisplay.class, display -> {
			String text = "&a&l[구매]";

			display.setPersistent(false);
			display.text(ColorUtils.chat(text));
			display.setBillboard(Display.Billboard.FIXED);
			display.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
			display.setShadowed(true);

			spawnedDisplay = display.getUniqueId();
		});

		return textLoc.getWorld().spawn(textLoc, Interaction.class, interaction -> {
			interaction.setInteractionWidth(0.8f);
			interaction.setInteractionHeight(0.5f);
			interaction.setPersistent(false);
			interaction.setResponsive(true);
			KeyUtils.set(interaction, key, goods.getName());

			spawnedInteraction = interaction.getUniqueId();
		});
	}

	private Location calculateFaceLocation(Location base, BlockFace face) {
		Location loc = base.clone().add(0.5, 0.5, 0.5);

		float yaw = 0f;
		float pitch = 0f;

		switch (face) {
			case NORTH -> { loc.add(0, 0, -0.51); yaw = 180f; }
			case SOUTH -> { loc.add(0, 0, 0.51); yaw = 0f; }
			case WEST  -> { loc.add(-0.51, 0, 0); yaw = 90f; }
			case EAST  -> { loc.add(0.51, 0, 0); yaw = 270f; }
			case UP    -> { loc.add(0, 0.51, 0); pitch = -90f; }
			case DOWN  -> { loc.add(0, -0.51, 0); pitch = 90f; }
			default    -> {}
		}

		loc.setYaw(yaw);
		loc.setPitch(pitch);
		return loc;
	}
}
