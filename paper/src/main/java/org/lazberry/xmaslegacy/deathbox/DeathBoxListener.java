package org.lazberry.xmaslegacy.deathbox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class DeathBoxListener implements Listener {
    private final DeathBoxManager dm;
    private final EconomyManager em;

    @Inject
    public DeathBoxListener(DeathBoxManager dm, EconomyManager em) {
        this.dm = dm;
        this.em = em;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void spawnDeathBoxWhenDead(PlayerDeathEvent e) {
        Player dead = e.getPlayer();
        Location loc = dead.getLocation();
        List<ItemStack> drops = e.getDrops();

        if (drops.isEmpty()) return;
        if (e.getKeepInventory()) {
            InfoUtils.warn(dead, "인벤토리가 유지됩니다.");
            return;
        }
        if (em.withdraw(dead.getUniqueId(), dm.getBoxCost())) {
            InfoUtils.info(dead, "사망 처리 비용 &6{}원&f이 출금되었습니다.", dm.getBoxCost());
            dm.dropDeathBox(dead, drops);
        } else {
            InfoUtils.error(dead, "잔액이 부족하여 전용 상자가 생성되지 않습니다.");
        }
        InfoUtils.warn(dead, "마지막 사망 위치 x: {} y: {} z: {} ({})",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

	@EventHandler
	public void openDeathBox(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		var clickedBlock = e.getClickedBlock();
		if (clickedBlock == null || clickedBlock.getType() != Material.CHEST) return;

		Location loc = clickedBlock.getLocation();

		dm.getDeathBox(loc).ifPresent(box -> {
			e.setCancelled(true);
			e.getPlayer().openInventory(box.getInventory());
		});
	}

    @EventHandler
    public void removeDeathBoxWhenEmpty(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof DeathBox box)) return;

        if (box.isEmpty() || box.isExpired()) {
            dm.removeDeathBox(box);
        }
    }

    @EventHandler
    public void removeDeathBoxWhenOpenIfEmpty(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof DeathBox box)) return;

        if (box.isEmpty() || box.isExpired()) {
            dm.removeDeathBox(box);
            e.setCancelled(true);
        }
    }
}
