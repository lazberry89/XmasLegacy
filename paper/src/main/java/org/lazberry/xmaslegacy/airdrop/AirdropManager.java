package org.lazberry.xmaslegacy.airdrop;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropManager implements Initiator {
	public static final NamespacedKey key = KeyUtils.get("airdrop");

	/**
	 * Only cache for cleaning up dummies. Not for restarting, and also
	 * every entity, tasks will be removed when close(shutdown, onDisable).
	 */
	private final Set<ArmorStand> onWorldStands = new HashSet<>();
	private final Set<BlockDisplay> onBlockDisplays = new HashSet<>();
	private final Set<BukkitRunnable> runningTasks = new HashSet<>();

	private final AirdropContentsManager contentsManager;
	private final XmasLegacy plugin;
	private final BlockData chest;
	private final Particle.DustTransition dustTransition;

	@Inject
	public AirdropManager(AirdropContentsManager contentsManager, XmasLegacy plugin) {
		this.contentsManager = contentsManager;
		this.plugin = plugin;
		this.chest = Material.WAXED_COPPER_CHEST.createBlockData();
		this.dustTransition = new Particle.DustTransition(Color.RED, Color.WHITE, 0.8f);
	}

	@Override
	public void init() {

	}

	public void spawnAirDrop(Location location) {
		World world = location.getWorld();
		int y = world.getMaxHeight();

		Location spawnLoc = location.clone().getBlock().getLocation();
		spawnLoc.setY(y);

		ArmorStand stand = spawnFallingDummy(spawnLoc);
		BlockDisplay display = spawnAirdropDisplay(spawnLoc);
		onWorldStands.add(stand);
		onBlockDisplays.add(display);

		new BukkitRunnable() {
			final Location reuse = new Location(world, 0, 0, 0);

			@Override
			public void run() {
				runningTasks.add(this);

				if (!stand.isValid() || !display.isValid()) {
					runningTasks.remove(this);
					this.cancel();
					return;
				}
				var standLoc = stand.getLocation(reuse);

				if (hasReached(stand)) {
					runningTasks.remove(this);
					onWorldStands.remove(stand);
					onBlockDisplays.remove(display);

					stand.remove();
					display.remove();

					placeAirdropChest(standLoc);
					this.cancel();
					return;
				}

				display.teleport(standLoc);
				world.spawnParticle(Particle.DUST_COLOR_TRANSITION, standLoc.add(0.5, 0, 0.5),
						3, 0.5, 0.5, 0.5, 0.01, dustTransition);
			}
		}.runTaskTimer(plugin, 3L, 1L);
	}

	private void placeAirdropChest(Location location) {
		var block = location.getBlock();
		block.setType(Material.WAXED_COPPER_CHEST, false);

		if (block.getState() instanceof Chest supplyChest) {
			supplyChest.setLockItem(new ItemStack(Material.OMINOUS_TRIAL_KEY));
			supplyChest.customName(ColorUtils.chat("보급상자"));
			supplyChest.update();

			setSupplyContents(supplyChest);
		} else {
			log.warn("Airdrop chest placement failed at {}", location);
		}
	}

	private boolean hasReached(ArmorStand stand) {
		Location loc = stand.getLocation();
		Location under = loc.clone().add(0, -1, 0);

		var block = under.getBlock();

		return block.isSolid();
	}

	private ArmorStand spawnFallingDummy(Location loc) {
		return loc.getWorld().spawn(loc, ArmorStand.class, s -> {
			s.setInvisible(true);
			s.setInvisible(true);
			s.setInvulnerable(true);
			s.setVisible(false);

			s.addPotionEffect(new PotionEffect(
					PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 1, true, false));
			KeyUtils.set(s, key, true);
		});
	}

	private BlockDisplay spawnAirdropDisplay(Location loc) {
		return loc.getWorld().spawn(loc, BlockDisplay.class, d -> {
			d.setGravity(false);
			d.teleport(loc);
			d.setBlock(chest);
			d.setGlowColorOverride(Color.RED);
			d.setGlowing(true);
			d.setTeleportDuration(3);
			KeyUtils.set(d, key, true);
		});
	}

	public void setSupplyContents(Chest chest) {
		Inventory inv = chest.getBlockInventory();
		ItemStack[] contents = new ItemStack[inv.getSize()];
		for (int i = 0; i < contents.length; i++) {
			contents[i] = contentsManager.getRandomAmountOfItem();
		}
		inv.setContents(contents);
	}

	private void cleanUp() {
		runningTasks.forEach(BukkitRunnable::cancel);
		runningTasks.clear();

		onWorldStands.forEach(stand -> {
			if (stand.isValid()) stand.remove();
		});
		onWorldStands.clear();

		onBlockDisplays.forEach(display -> {
			if (display.isValid()) display.remove();
		});
		onBlockDisplays.clear();
	}

	@Override
	public void close() {
		cleanUp();
	}
}
