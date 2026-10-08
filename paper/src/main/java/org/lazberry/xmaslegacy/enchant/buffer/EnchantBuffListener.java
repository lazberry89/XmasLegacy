package org.lazberry.xmaslegacy.enchant.buffer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.enchant.listener.PlayerEnchantEvent;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantBuffListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void applyBoosterToItem(PlayerEnchantEvent e) {
        var item = e.getItem();
        EnchantBuffHandler.checkIfBuffedAndGet(item, amount -> {
            if (amount == null || amount <= 0.0) return;
            e.setBonusChance(amount);
            EnchantBuffHandler.removeBuffTag(item);
        });
    }

    @EventHandler
    public void useEnchantBuffer(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        var cursor = e.getCursor();
        var current = e.getCurrentItem();

        if (cursor.getType().isAir() || current == null || current.getType().isAir()) return;
        if (!EnchantBuffHandler.isBuffer(cursor)) return;

        int value = KeyUtils.get(current, EnchantManager.key, 0);
        e.setCancelled(true);
        if (value == 0) {
            InfoUtils.error(p, "강화전용 아이템이 아닙니다.");
            return;
        }
        if (value == 10) {
            InfoUtils.error(p, "이미 최대로 강화된 장비입니다!");
            return;
        }
        if (EnchantBuffHandler.isBuffedTool(current)) return;
        double amount = EnchantBuffHandler.getBufferAmount(cursor);

        if (amount <= 0.0) return;
        if (EnchantBuffHandler.setBuffed(current, amount)) {
            p.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.4f);
            cursor.setAmount(cursor.getAmount() - 1);
        }
    }
}
