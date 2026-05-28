package xmasLegacy.Enchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

import java.util.Objects;

public class EnchantListener implements Listener {
    private final XmasLegacy plugin;
    private final EnchantManager ecm;

    public EnchantListener() {
        this.plugin = XmasLegacy.getInstance();
        this.ecm = EnchantManager.getInstance();
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


            // TODO 강화서 체크로직

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
}