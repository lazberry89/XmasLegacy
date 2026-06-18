package xmaslegacy;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.Enchant.EnchantUserInterface;
import xmaslegacy.Utils.InfoLevel;

import java.util.List;

@Listeners
public class GlobalListeners implements Listener {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull NamespacedKey key;
	private final @NotNull NamespacedKey key2;

    public GlobalListeners() {
        this.plugin = XmasLegacy.getInstance();
        this.key = plugin.getNamespacedKey("role_id");
		this.key2 = plugin.getNamespacedKey("emblem_type");
    }

    private boolean isCombatItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
		var meta = item.getItemMeta();
		if (meta == null) return false;
		var container = meta.getPersistentDataContainer();
	    return container.has(key) || container.has(key2);
    }

    @EventHandler
    public void removeCombatItems(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        List<ItemStack> remain = e.getDrops();

        if (e.getKeepInventory()) return;
        if (remain.isEmpty()) return;

        remain.removeIf(this::isCombatItem);
        plugin.infoMsg(InfoLevel.WARN, victim, "직업관련 아이템은 소멸합니다.");
    }

    @EventHandler
    public void blockCombatItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();

        if (isCombatItem(item)) {
            e.setCancelled(true);
            plugin.infoMsg(InfoLevel.WARN, p, "직업 아이템은 버릴 수 없습니다.");
        }
    }

    @EventHandler
    public void blockItemFrame(PlayerItemFrameChangeEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemStack();

        if (isCombatItem(item)) {
            e.setCancelled(true);
            plugin.infoMsg(InfoLevel.WARN, p, "꼼수 ㄴㄴ");
        }
    }

    @EventHandler
    public void blockInventoryClick(InventoryClickEvent e) {
        Inventory topInv = e.getView().getTopInventory();

        if (topInv.getType() == InventoryType.PLAYER
                || topInv.getType() == InventoryType.CRAFTING
                || topInv.getHolder() instanceof EnchantUserInterface) return; //TODO 이후 상점 혹은 강화 인터페이스만 허용(getHolder()사용)

        Player p = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if (e.getClickedInventory() == e.getView().getBottomInventory()) {
            if (e.getClick().isShiftClick() && isCombatItem(current)) {
                e.setCancelled(true);
                plugin.infoMsg(InfoLevel.WARN, p, "직업 아이템은 다른 보관함에 넣을 수 없습니다.");
            }
            return;
        }

        if (e.getClickedInventory() == topInv) {
            if (isCombatItem(cursor)) {
                e.setCancelled(true);
                plugin.infoMsg(InfoLevel.WARN, p, "직업 item은 다른 보관함에 넣을 수 없습니다.");
                return;
            }
            if (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
                org.bukkit.inventory.ItemStack hotbarItem = p.getInventory().getItem(e.getHotbarButton());
                if (isCombatItem(hotbarItem)) {
                    e.setCancelled(true);
                    plugin.infoMsg(InfoLevel.WARN, p, "직업 아이템은 다른 보관함에 넣을 수 없습니다.");
                }
            }
        }
    }

    @EventHandler
    public void blockInventoryDrag(InventoryDragEvent e) {
        Inventory topInv = e.getView().getTopInventory();

        if (topInv.getType() == InventoryType.PLAYER
                || topInv.getType() == InventoryType.CRAFTING) return;

        if (!isCombatItem(e.getOldCursor())) return;

        for (int rawSlot : e.getRawSlots()) {
            if (rawSlot < topInv.getSize()) {
                e.setCancelled(true);
                plugin.infoMsg(InfoLevel.WARN, (Player) e.getWhoClicked(), "꼼수 ㄴㄴ");
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory topInv = e.getView().getTopInventory();

        if (topInv.getType() == InventoryType.PLAYER
                || topInv.getType() == InventoryType.CRAFTING) return;

        Player p = (Player) e.getPlayer();

        for (ItemStack item : topInv.getContents()) {
            if (!isCombatItem(item)) continue;

            topInv.remove(item);
            p.getInventory().addItem(item);
            plugin.infoMsg(InfoLevel.WARN, p, "보관함에 남겨진 직업 아이템을 강제 회수했습니다.");
        }
    }
}
