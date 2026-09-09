package org.lazberry.xmaslegacy;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.List;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class GlobalListeners implements Listener {
    private final @NotNull NamespacedKey key;
	private final @NotNull NamespacedKey key2;

    public GlobalListeners() {
        this.key = KeyUtils.get("role_id");
		this.key2 = KeyUtils.get("emblem_type");
    }

    private boolean isCombatItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
		var meta = item.getItemMeta();
		if (meta == null) return false;;
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
        InfoUtils.warn(victim, "직업관련 아이템은 소멸합니다.");
    }

    @EventHandler
    public void blockCombatItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();

        if (isCombatItem(item)) {
            e.setCancelled(true);
            InfoUtils.warn(p, "직업 아이템은 버릴 수 없습니다.");
        }
    }

    @EventHandler
    public void blockItemFrame(PlayerItemFrameChangeEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemStack();

        if (isCombatItem(item)) {
            e.setCancelled(true);
            InfoUtils.warn(p, "꼼수 ㄴㄴ");
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
                InfoUtils.warn((Player) e.getWhoClicked(), "꼼수 ㄴㄴ");
                return;
            }
        }
    }

	/**
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory topInv = e.getView().getTopInventory();

        if (topInv.getType() == InventoryType.PLAYER
                || topInv.getType() == InventoryType.CRAFTING
                || topInv.getHolder() instanceof RoleSelectInterface
                || topInv.getHolder() instanceof RoleSelectionInterface) return;

        Player p = (Player) e.getPlayer();

        for (ItemStack item : topInv.getContents()) {
            if (!isCombatItem(item)) continue;

            topInv.remove(item);
            p.getInventory().addItem(item);
            InfoUtils.warn(p, "보관함에 남겨진 직업 아이템을 강제 회수했습니다.");
        }
    }
    **/
}
