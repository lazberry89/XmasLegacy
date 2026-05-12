package xmasLegacy.Gacha;

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
        if (!(e.getInventory().getHolder() instanceof GachaStockInterface nsi)) return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);

        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 관리자만 재고를 꺼낼 수 있어요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        BundleType type = nsi.getBundleType();

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;
        NamespacedKey nameKey = plugin.getNamespacedKey("gacha");
        String key = item.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
        Gacha gacha = GM.getGacha(key, type);
        if (gacha == null) return;

        ItemStack result = gacha.getItem();
        p.getInventory().addItem(result);
    }

    @EventHandler
    public void BundleChoose(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof BundleTypeInterface)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack click = e.getCurrentItem();

        e.setCancelled(true);
        if (click == null) return;

        try {
            String value = click.getPersistentDataContainer().get(plugin.getNamespacedKey("gacha"), PersistentDataType.STRING);
            BundleType type = BundleType.valueOf(value);
            p.openInventory(new GachaStockInterface(plugin, type).getInventory());
            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            p.updateInventory();

        } catch (IllegalArgumentException er) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 해당 확률형 아이템의 형식이 잘못되었습니다."));
            plugin.getSLF4JLogger().warn("Bundle build Error! ", er);
        }
    }
}