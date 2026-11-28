package org.lazberry.xmaslegacy.catalog;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.advancement.Advancements;
import org.lazberry.xmaslegacy.catalog.database.CatalogSaveManager;
import org.lazberry.xmaslegacy.catalog.database.SqlCatalogRepository;
import org.lazberry.xmaslegacy.catalog.gui.CatalogInterface;
import org.lazberry.xmaslegacy.catalog.gui.CatalogSampleEditor;
import org.lazberry.xmaslegacy.catalog.gui.CatalogSelectInterface;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventorySerializer;
import io.th0rgal.oraxen.api.OraxenItems;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Samples (which item belongs to which slot, edited by admins) and every online player's progress.
 * Everything except {@link #loadSamples()} runs on the main thread.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogManager implements Initiator {
	public static final int SIZE = 27;
	static final String SAMPLE_CHANNEL = "catalog_sample_updated";
	private static final ItemStack[] EMPTY = new ItemStack[SIZE];
	/** Catalogs that count towards catalog_all, with the advancement of each. */
	private static final Map<CatalogType, String> COMPLETE_ADVANCEMENTS = Map.of(
			CatalogType.FARM, "catalog_farm",
			CatalogType.FISH, "catalog_fish",
			CatalogType.MINE, "catalog_mine",
			CatalogType.HUNT, "catalog_hunt"
	);

	private final Map<CatalogType, ItemStack[]> samples = new ConcurrentHashMap<>();
	private final Map<UUID, CatalogProgress> progresses = new ConcurrentHashMap<>();
	private final Set<CatalogType> editing = EnumSet.noneOf(CatalogType.class);
	private final XmasLegacy plugin;
	private final SqlCatalogRepository repository;
	private final CatalogSaveManager saveManager;

	@Inject
	public CatalogManager(XmasLegacy plugin, SqlCatalogRepository repository, CatalogSaveManager saveManager) {
		this.plugin = plugin;
		this.repository = repository;
		this.saveManager = saveManager;
	}

	@Override
	public void init() {
		loadSamples();
		Bukkit.getOnlinePlayers().forEach(this::load);
	}

	// ---- samples ----

	void loadSamples() {
		repository.loadSampleCatalog().whenComplete((stored, ex) -> {
			if (ex != null) {
				log.error("Failed to load catalog samples; keeping the current ones.", ex);
				return;
			}
			stored.forEach((type, data) -> {
				try {
					samples.put(type, decode(data));
				} catch (Exception e) {
					log.error("Broken catalog sample '{}', skipped.", type, e);
				}
			});
		});
	}

	private static ItemStack[] decode(@Nullable String data) throws Exception {
		return cloneContents(InventorySerializer.deserializeContents(data));
	}

	/** Deep copy with exactly {@link #SIZE} slots; air becomes null. */
	public static ItemStack[] cloneContents(@Nullable ItemStack[] contents) {
		ItemStack[] copy = new ItemStack[SIZE];
		for (int i = 0; contents != null && i < Math.min(SIZE, contents.length); i++) {
			if (contents[i] != null && !contents[i].isEmpty()) copy[i] = contents[i].clone();
		}
		return copy;
	}

	private ItemStack[] samplesOf(CatalogType type) {
		return samples.getOrDefault(type, EMPTY);
	}

	/** Reads the sample from the database, so an edit never starts from another server's outdated copy. */
	public void openSampleEditor(@NotNull Player player, @NotNull CatalogType type) {
		if (!editing.add(type)) {
			InfoUtils.error(player, "다른 관리자가 수정 중인 도감입니다.");
			return;
		}
		repository.loadSampleCatalog().whenComplete((stored, ex) -> runSync(() -> {
			try {
				if (ex != null) throw new IllegalStateException(ex);
				if (!player.isOnline()) {
					editing.remove(type);
					return;
				}
				var editor = new CatalogSampleEditor(type, decode(stored.get(type)));
				if (player.openInventory(editor.getInventory()) == null) editing.remove(type); // blocked: no close event will come
			} catch (Exception e) {
				editing.remove(type);
				log.error("Failed to open catalog sample '{}'", type, e);
				InfoUtils.error(player, "샘플을 불러오지 못했습니다. 서버 로그를 확인하세요.");
			}
		}));
	}

	public void closeSampleEditor(@NotNull Player player, @NotNull CatalogSampleEditor editor) {
		CatalogType type = editor.getType();
		if (!editor.isChanged()) {
			editing.remove(type);
			return;
		}
		ItemStack[] contents = editor.contents();
		repository.saveSampleCatalog(Map.of(type, contents)).whenComplete((v, ex) -> runSync(() -> {
			if (ex != null) {
				log.error("Failed to save catalog sample '{}'", type, ex);
				if (player.isOnline()) {
					// Keeps the edit: the window opens again with the same contents, still locked for others.
					if (player.openInventory(editor.getInventory()) == null) editing.remove(type);
					InfoUtils.error(player, "샘플 저장에 실패했습니다. 창을 다시 닫으면 재시도합니다.");
				} else editing.remove(type);
				return;
			}
			editing.remove(type);
			samples.put(type, contents);
			InfoUtils.info(player, "{} 샘플을 저장했습니다.", type.getDisplayName());
			try {
				JedisService.publish(SAMPLE_CHANNEL, type.name());
			} catch (RuntimeException e) {
				log.error("Could not tell the other servers about the new catalog sample.", e);
			}
		}));
	}

	// ---- players ----

	public void load(@NotNull Player player) {
		UUID uuid = player.getUniqueId();
		saveManager.loadWhenJoin(uuid).thenAccept(progress -> runSync(() -> {
			if (!player.isOnline()) return;
			if (progress == null) InfoUtils.error(player, "도감을 불러오지 못했습니다. 잠시 후 다시 접속해주세요.");
			// putIfAbsent: a second load must not undo what was registered meanwhile.
			else progresses.putIfAbsent(uuid, progress);
		}));
	}

	public void unload(@NotNull UUID uuid) {
		progresses.remove(uuid);
	}

	public int total(@NotNull CatalogType type) {
		int total = 0;
		for (ItemStack sample : samplesOf(type)) if (sample != null) total++;
		return total;
	}

	/** Only slots that still have a sample count, so removing a sample cannot push this above the total. */
	public int registered(@NotNull CatalogType type, @NotNull CatalogProgress progress) {
		ItemStack[] slots = samplesOf(type);
		int count = 0;
		for (int i = 0; i < SIZE; i++) if (slots[i] != null && progress.isRegistered(type, i)) count++;
		return count;
	}

	public boolean isComplete(@NotNull CatalogType type, @NotNull CatalogProgress progress) {
		int total = total(type);
		return total > 0 && registered(type, progress) == total;
	}

	private @Nullable CatalogProgress progressOrWarn(Player player) {
		CatalogProgress progress = progresses.get(player.getUniqueId());
		if (progress == null) {
			InfoUtils.warn(player, "도감을 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
			load(player);
		}
		return progress;
	}

	public void openSelection(@NotNull Player player) {
		CatalogProgress progress = progressOrWarn(player);
		if (progress != null) player.openInventory(new CatalogSelectInterface(this, progress).getInventory());
	}

	public void open(@NotNull Player player, @NotNull CatalogType type) {
		if (total(type) == 0) {
			InfoUtils.warn(player, "아직 준비 중인 도감입니다.");
			return;
		}
		CatalogProgress progress = progressOrWarn(player);
		if (progress == null) return;

		var gui = new CatalogInterface(player, type);
		gui.render(samplesOf(type), progress);
		player.openInventory(gui.getInventory());
		grantAdvancements(player, progress);
	}

	/** Registers the clicked item, or with {@code all} every matching item the player carries. Each slot consumes one item. */
	public void register(@NotNull Player player, @NotNull CatalogInterface gui, int clickedSlot, boolean all) {
		CatalogProgress current = progressOrWarn(player);
		if (current == null) return;

		CatalogType type = gui.getType();
		ItemStack[] slots = samplesOf(type);
		Inventory inventory = player.getInventory();
		CatalogProgress updated = current.copy();
		Map<Integer, Integer> consumed = new HashMap<>();

		for (int i = all ? 0 : clickedSlot; i < (all ? 36 : clickedSlot + 1); i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || item.isEmpty()) continue;
			for (int slot = 0; slot < SIZE && consumed.getOrDefault(i, 0) < item.getAmount(); slot++) {
				if (matches(slots[slot], item) && updated.register(type, slot)) consumed.merge(i, 1, Integer::sum);
			}
		}
		if (consumed.isEmpty()) {
			InfoUtils.error(player, "이미 등록했거나 이 도감에 없는 아이템입니다.");
			player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		try {
			saveManager.save(player.getUniqueId(), updated); // before consuming: a failed save must cost nothing
		} catch (RuntimeException e) {
			log.error("Failed to save catalog of {}", player.getName(), e);
			InfoUtils.error(player, "도감을 저장하지 못했습니다. 잠시 후 다시 시도해주세요.");
			return;
		}
		progresses.put(player.getUniqueId(), updated);
		consumed.forEach((i, amount) -> inventory.setItem(i, shrink(inventory.getItem(i), amount)));

		int count = consumed.values().stream().mapToInt(Integer::intValue).sum();
		InfoUtils.info(player, "{}에 &6{}개&f를 등록했습니다. &7({}/{})", type.getDisplayName(), count, registered(type, updated), total(type));
		player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
		gui.render(slots, updated);
		Advancements.grant(player, "catalog_first");
		grantAdvancements(player, updated);
	}

	private static @Nullable ItemStack shrink(@Nullable ItemStack item, int amount) {
		if (item == null || item.getAmount() <= amount) return null;
		ItemStack left = item.clone();
		left.setAmount(item.getAmount() - amount);
		return left;
	}

	/** Oraxen items by id (their lore can differ per copy), everything else exactly. */
	private static boolean matches(@Nullable ItemStack sample, @NotNull ItemStack item) {
		if (sample == null) return false;
		String id = Bukkit.getPluginManager().isPluginEnabled("Oraxen") ? OraxenItems.getIdByItem(sample) : null;
		return id != null ? id.equals(OraxenItems.getIdByItem(item)) : sample.isSimilar(item);
	}

	private void grantAdvancements(Player player, CatalogProgress progress) {
		int complete = 0;
		for (var entry : COMPLETE_ADVANCEMENTS.entrySet()) {
			if (!isComplete(entry.getKey(), progress)) continue;
			complete++;
			Advancements.grant(player, entry.getValue());
		}
		if (complete == COMPLETE_ADVANCEMENTS.size()) Advancements.grant(player, "catalog_all");
	}

	private void runSync(Runnable action) {
		if (plugin.isEnabled()) Bukkit.getScheduler().runTask(plugin, action);
	}

	/** Listeners are already dead here: windows left open would let players take the shown items. */
	@Override
	public void close() {
		for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
			InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
			if (holder instanceof CatalogSampleEditor editor && editor.isChanged()) {
				try {
					repository.saveSampleCatalog(Map.of(editor.getType(), editor.contents())).join();
				} catch (RuntimeException e) {
					log.error("Failed to save catalog sample '{}' on shutdown", editor.getType(), e);
				}
			}
			if (holder instanceof CatalogSampleEditor || holder instanceof CatalogInterface || holder instanceof CatalogSelectInterface)
				player.closeInventory();
		}
		progresses.clear();
		samples.clear();
	}
}
