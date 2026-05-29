package xmasLegacy.Enchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.InfoLevel;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.XmasLegacy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class EnchantListener implements Listener {
	private final XmasLegacy plugin;
	private final EnchantManager ecm;
	private final BagManager bm;

	public EnchantListener() {
		this.plugin = XmasLegacy.getInstance();
		this.ecm = EnchantManager.getInstance();
		this.bm = BagManager.getInstance();
	}

	@EventHandler
	public void enhance(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;

		Inventory topInv = e.getView().getTopInventory();
		if (!(topInv.getHolder() instanceof EnchantUserInterface eui)) return;

		Inventory clickedInv = e.getClickedInventory();
		if (clickedInv == null) return;

		int slot = e.getSlot();

		if (clickedInv.equals(topInv)) {
			if (slot != 13) e.setCancelled(true);

			if (slot == 22) {
				ItemStack item = topInv.getItem(13);
				if (item == null || item.getType() == Material.AIR) {
					plugin.infoMsg(InfoLevel.ERROR, p, "강화할 아이템을 먼저 올려주세요.");
					return;
				}
				if (!ecm.isEnchantable(item)) {
					plugin.infoMsg(InfoLevel.ERROR, p, "강화 가능한 아이템이 아니에요!");
					return;
				}

				int origin = Objects.requireNonNullElse(ecm.getEnchantLevel(item), 1);

				if (origin - 1 >= Constants.ENCHANT_NEEDED.size()) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 최대 등급까지 강화된 장비입니다!"));
					return;
				}
				int neededAmount = Constants.ENCHANT_NEEDED.get(origin - 1);

				Inventory bottomInv = e.getView().getBottomInventory();
				int totalMaterials = Arrays.stream(bottomInv.getContents())
						.filter(Objects::nonNull)
						.filter(EnchantMaterial::isMaterial)
						.mapToInt(ItemStack::getAmount)
						.sum();

				if (totalMaterials < neededAmount) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 강화에 필요한 재료가 부족해요! (필요: " + neededAmount + "개)"));
					return;
				}

				int leftToRemove = neededAmount;
				ItemStack[] contents = bottomInv.getContents();
				for (int i = 0; i < contents.length; i++) {
					ItemStack invItem = contents[i];
					if (invItem == null || invItem.getType() == Material.AIR) continue;

					if (EnchantMaterial.isMaterial(invItem)) {
						int amt = invItem.getAmount();
						if (amt >= leftToRemove) {
							invItem.setAmount(amt - leftToRemove);
							leftToRemove = 0;
							break;
						} else {
							leftToRemove -= amt;
							bottomInv.setItem(i, null);
						}
					}
				}

				ResultType result = ecm.enchant(item);
				int lvl = Objects.requireNonNullElse(ecm.getEnchantLevel(item), 1);
				int diff = lvl - origin;

				switch (result) {
					case SUCCEED -> {
						if (lvl >= 10) {
							Bukkit.broadcast(ColorUtils.chat("&6&l------------------------------------"));
							Bukkit.broadcast(ColorUtils.chat(String.format("[ %s ] 님이 \"%s\" 장비에 &6&l10강 강화&f를 성공하였어요!", p.getName(), item.getType().name())));
							Bukkit.broadcast(ColorUtils.chat("&6&l------------------------------------"));
							p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
							p.getWorld().playSound(p, Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
						}
						p.sendMessage(ColorUtils.chat(String.format("%s &6%d강&f 강화에 &a성공&f하였습니다! &a&l[+%d]", Alert.XmasLegacy, lvl, diff)));
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
					}
					case FAIL -> {
						p.sendMessage(ColorUtils.chat(String.format("%s 강화에 &c실패&f하였습니다. %s", Alert.XmasLegacy, diff == 0 ? "&7&l[-]" : "&c&l[" + diff + "]")));
						p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
					}
					case BREAK -> {
						p.sendMessage(ColorUtils.chat("&c&l장비가 파괴되었습니다!"));
						topInv.setItem(13, null);
						p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
						p.playSound(p, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
					}
					default -> plugin.getSLF4JLogger().error("Error occurred while selecting Result. (EnchantListener.class, enhance())");
				}
			}
		}
		else {
			if (e.isShiftClick()) e.setCancelled(true);
		}

		Bukkit.getScheduler().runTask(plugin, () -> eui.updateInv(p));
	}

	@EventHandler
	public void giveBack(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player p)) return;
		Inventory topInv = e.getView().getTopInventory();

		if (!(topInv.getHolder() instanceof EnchantUserInterface)) return;
		ItemStack item = topInv.getItem(13);
		if (item != null && item.getType() != Material.AIR) {
			HashMap<Integer, ItemStack> remain = p.getInventory().addItem(item);
			if (!remain.isEmpty()) {
				remain.values().forEach(i -> bm.addItem(p, i, i.getAmount()));
				p.sendMessage(ColorUtils.chat(Alert.GREEN + " 나머지는 가방에 보관되었어요."));
			}

			topInv.setItem(13, null);
			p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 장비 가져가셔야죠?"));
		}
	}

	@EventHandler
	public void enchantDamager(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player p)) return;
		if (!(e.getEntity() instanceof LivingEntity victim)) return;

		ItemStack weapon = p.getInventory().getItemInMainHand();
		if (weapon.getType().isAir()) return;

		ItemMeta meta = weapon.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer container = meta.getPersistentDataContainer();
		Integer value = container.get(plugin.getNamespacedKey("enchant"), PersistentDataType.INTEGER);
		if (value == null) return;

		// 🌟 [안전장치] 레벨 값이 비정상적이거나 0 이하일 때의 예외 방지
		if (value < 1 || value - 1 >= Constants.ENCHANT_MULTIPLIERS.size()) return;

		double damage = e.getFinalDamage();
		Double multiple = Constants.ENCHANT_MULTIPLIERS.get(value - 1);
		if (multiple == null) return;

		double finalDamage = damage * multiple;
		e.setDamage(finalDamage);

		if (value >= 8) victim.setFireTicks(60);
		Random random = new Random();
		if (value >= 10) {
			if (random.nextInt(10) == 1) {
				victim.damage(random.nextInt(10));
				victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
			}
		}
	}
}