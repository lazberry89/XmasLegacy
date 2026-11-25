package org.lazberry.xmaslegacy.casino.games.CoinFlip;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Listeners
@Components.Include(type = ServerType.MAIN)
public class CoinFlipListener implements Listener {
	private final CoinFlipManager manager;
	private final XmasLegacy plugin;

	@Inject
	public CoinFlipListener(CoinFlipManager manager, XmasLegacy plugin) {
		this.manager = manager;
		this.plugin = plugin;
	}

	@EventHandler
	public void openTable(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
		manager.getMachineByBlock(e.getClickedBlock().getLocation()).ifPresent(machine -> {
			e.setCancelled(true);
			Player p = e.getPlayer();
			p.openInventory(new CoinFlipInterface(plugin, machine.getId()).getInventory());
		});
	}

	@EventHandler
	public void preventBreak(BlockBreakEvent e) {
		if (e.getPlayer().isOp()) return;
		if (manager.getMachineByBlock(e.getBlock().getLocation()).isPresent()) e.setCancelled(true);
	}

	@EventHandler
	public void click(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
		if (!(e.getView().getTopInventory().getHolder() instanceof CoinFlipInterface table)) return;

		// Bottom inventory clicks are free (picking up coins), except shift-click which could fill other slots.
		if (e.getClickedInventory() != e.getView().getTopInventory()) {
			if (e.isShiftClick()) e.setCancelled(true);
			return;
		}
		int slot = e.getRawSlot();
		if (slot == CoinFlipInterface.STAKE_SLOT) return;
		e.setCancelled(true);
		if (slot != CoinFlipInterface.HEADS_SLOT && slot != CoinFlipInterface.TAILS_SLOT) return;

		int stake = table.getStake();
		if (stake <= 0) {
			InfoUtils.error(p, "가운데 칸에 카지노 코인을 올려주세요!");
			return;
		}
		CoinFlip.State pick = slot == CoinFlipInterface.HEADS_SLOT ? CoinFlip.State.FRONT : CoinFlip.State.BACK;
		table.takeStakeSlot();
		Bukkit.getScheduler().runTask(plugin, () -> p.closeInventory(InventoryCloseEvent.Reason.PLUGIN));
		manager.flip(p, table.getMachineId(), pick, stake, true);
	}

	@EventHandler
	public void returnStakeOnClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player p)) return;
		if (!(e.getInventory().getHolder() instanceof CoinFlipInterface table)) return;
		ItemStack left = table.takeStakeSlot();
		if (left == null || left.getType().isAir()) return;
		InventoryHelper.giveItemOrKeep(p, left);
		InfoUtils.warn(p, "올려둔 아이템이 회수되었습니다.");
	}
}
