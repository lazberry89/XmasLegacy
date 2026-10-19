package org.lazberry.xmaslegacy.airdrop;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.airdrop.event.AirdropLandEvent;
import org.lazberry.xmaslegacy.airdrop.event.AirdropSpawnEvent;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.Axiom;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter @Setter
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
	private final AirdropConfig config;
	private final XmasLegacy plugin;
	private final Particle.DustTransition dustTransition;

	private boolean dropOnLiquid = false;
	private int tickPerProcess = 3;
	private Material chestMaterial = Material.WAXED_COPPER_CHEST;
	private BlockData chestData = chestMaterial.createBlockData();

	@Inject
	public AirdropManager(AirdropContentsManager contentsManager, AirdropConfig config, XmasLegacy plugin) {
		this.contentsManager = contentsManager;
        this.config = config;
        this.plugin = plugin;
		this.dustTransition = new Particle.DustTransition(Color.RED, Color.WHITE, 0.8f);
	}

	@Override
	public void init() {
		cleanUp();
		reload().join();

		this.chestData = chestMaterial.createBlockData();
	}

	public CompletableFuture<Void> reload() {
		return CompletableFuture.runAsync(() -> {
			synchronized (this) {
				dropOnLiquid = config.getDropOnLiquid();
				tickPerProcess = config.getTickPerProcess();
				chestMaterial = config.getAirdropMaterial();
			}
		});
	}

	public boolean isSupplyChest(Block block) {
		return block instanceof Chest supply && KeyUtils.hasKey(supply, key);
	}

	public void spawnAirDrop(Location location) {
		World world = location.getWorld();
		int y = world.getMaxHeight();

		Location spawnLoc = location.clone().getBlock().getLocation();
		spawnLoc.setY(y);

		var spawnEvent = new AirdropSpawnEvent(spawnLoc);
		Bukkit.getPluginManager().callEvent(spawnEvent);

		if (spawnEvent.isCancelled()) return;
		spawnLoc = spawnEvent.getLocation();

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
				standLoc.getChunk().load(true);

				if (standLoc.getBlock().isLiquid()) {
					stand.removePotionEffect(PotionEffectType.SLOW_FALLING);
				}

				if (hasReached(stand)) {
					runningTasks.remove(this);
					onWorldStands.remove(stand);
					onBlockDisplays.remove(display);

					stand.remove();
					display.remove();

					Chest airdrop = placeAirdropChest(standLoc);
					var blockInv = airdrop.getBlockInventory();

					var landEvent = new AirdropLandEvent(airdrop.getLocation(), airdrop, blockInv.getContents());
					Bukkit.getPluginManager().callEvent(landEvent);
					blockInv.setContents(landEvent.getContents());

					playLandingEffect(standLoc);

					this.cancel();
					return;
				}

				display.teleport(standLoc);
				world.spawnParticle(Particle.DUST_COLOR_TRANSITION, standLoc.add(0.5, 0, 0.5),
						3, 0.5, 0.5, 0.5, 0.01, dustTransition);
			}
		}.runTaskTimer(plugin, 3L, tickPerProcess);
	}

	private void playLandingEffect(Location loc) {
		var center = Axiom.centerBlock(loc);
		var world = center.getWorld();

		world.playSound(center, Sound.BLOCK_COPPER_GOLEM_STATUE_STEP, 1.0f, 0.3f);
		world.playSound(center, Sound.BLOCK_COPPER_BREAK, 0.8f, 0.5f);
		world.spawnParticle(Particle.ASH, center, 10, 0.6, 0.6, 0.6, 0.01);
	}

	private Chest placeAirdropChest(Location location) {
		var block = location.getBlock();
		block.setType(chestMaterial, false);

		if (block.getState() instanceof Chest supplyChest) {
			supplyChest.setLockItem(new ItemStack(Material.OMINOUS_TRIAL_KEY));
			supplyChest.customName(ColorUtils.chat("&c보급상자"));
			supplyChest.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
			supplyChest.update();

			setSupplyContents(supplyChest);
			return supplyChest;
		} else {
			throw new IllegalStateException("Airdrop chest placement failed at " + location);
		}
	}

	private boolean hasReached(ArmorStand stand) {
		Location loc = stand.getLocation();
		Location under = loc.clone().add(0, -0.05, 0);

		var block = under.getBlock();

		return block.isSolid() || (dropOnLiquid && block.isLiquid());
	}

	private ArmorStand spawnFallingDummy(Location loc) {
		var spawnLoc = Axiom.toCenterLocation(loc);
		return spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class, s -> {
			s.setInvisible(true);
			s.setInvisible(true);
			s.setInvulnerable(true);
			s.setVisible(false);
			s.setSmall(true);
			s.setRemoveWhenFarAway(false);

			s.addPotionEffect(new PotionEffect(
					PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 1, true, false));
			KeyUtils.set(s, key, true);
		});
	}

	private BlockDisplay spawnAirdropDisplay(Location loc) {
		var spawnLoc = Axiom.toCenterLocation(loc);
		return spawnLoc.getWorld().spawn(spawnLoc, BlockDisplay.class, d -> {
			d.setGravity(false);
			d.teleport(loc);
			d.setBlock(chestData);

			var transformation = d.getTransformation();
			transformation.getTranslation().set(-0.5f, 0f, -0.5f);
			d.setTransformation(transformation);

			d.setGlowColorOverride(Color.RED);
			d.setGlowing(true);
			d.setTeleportDuration(tickPerProcess);
			KeyUtils.set(d, key, true);
		});
	}

	private void setSupplyContents(Chest chest) {
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
