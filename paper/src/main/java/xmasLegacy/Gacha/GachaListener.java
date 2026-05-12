package xmasLegacy.Gacha;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

public class GachaListener implements Listener {
    private final XmasLegacy plugin;
    private final GachaManager GM;

    public GachaListener(XmasLegacy plugin) {
        this.plugin = plugin;
        this.GM = plugin.GM;
    }

    @EventHandler
    public void onStockClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof GachaStockInterface)) return;
        Player p = (Player) e.getWhoClicked();
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 관리자만 재고를 꺼낼 수 있어요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;
        NamespacedKey nameKey = plugin.getNamespacedKey("gacha");
        String key = item.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
        Gacha gacha = GM.getGacha(key);
        if (gacha == null) return;

        ItemStack result = gacha.getItem();
        p.getInventory().addItem(result);
    }
}
