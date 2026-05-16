package xmasLegacy.Gacha;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.InfoLevel;
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
            p.sendMessage(ColorUtils.chat(Alert.RED + " 관리자만 재고를 꺼낼 수 있어요!"));
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
            p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 확률형 아이템의 형식이 잘못되었습니다."));
            plugin.getSLF4JLogger().warn("Bundle build Error! ", er);
        }
    }

    @EventHandler
    public void onBundleUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!e.getAction().isRightClick()) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = plugin.getNamespacedKey("gacha");
        String value = container.get(key, PersistentDataType.STRING);
        if (value == null) return;
        try {
            BundleType grade = BundleType.valueOf(value);
            this.Random(p, grade);
        } catch (IllegalArgumentException err) {
            plugin.getSLF4JLogger().error("Bundle type setting error : {}", value, err);
            plugin.infoMsg(InfoLevel.ERROR, p, "문제가 발생했습니다. 관리자에게 문의하세요.");
        }
    }
    @Contract(pure = true)
    private void Random(@NotNull Player p, @NotNull BundleType type) {
        Gacha gacha = GM.getRandomItem(type);
        if (gacha == null) {
            plugin.infoMsg(InfoLevel.ERROR, p, "현재 등록된 아이템이 없어요! 관리자에게 문의하세요.");
            return;
        }
        GachaGrade grade = gacha.getGrade();
        p.getInventory().addItem(gacha.getItem());
        switch (grade) {
            case NORMAL, RARE -> plugin.infoMsg(InfoLevel.INFO, p, "아이템을 획득하였습니다! " + grade.getColor() + grade.name() + "[" + gacha.getKey() + "]");
            case MYTHIC -> {
                p.sendMessage(" ");
                p.sendMessage(ColorUtils.chat("  &4&l[!] &e&l신화급 아이템을 발견했습니다."));
                p.sendMessage(" ");
                p.sendMessage(ColorUtils.chat("    &7아이템 &8:: " + grade.getColor() + grade.name() + " &f" + gacha.getKey()));
                p.sendMessage(ColorUtils.chat("    &7상태 &8:: &a획득 완료"));
                p.sendMessage(" ");

                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            case LEGENDARY -> {
                p.sendMessage(ColorUtils.chat("&6&m----------------------------------------------------"));
                p.sendMessage(" ");
                p.sendMessage(ColorUtils.chat("          &e&l전설 아이템을 획득하였습니다!"));
                p.sendMessage(" ");
                p.sendMessage(ColorUtils.chat("      &f등급 &8| &6&l" + grade.name()));
                p.sendMessage(ColorUtils.chat("      &f이름 &8| &e" + gacha.getKey()));
                p.sendMessage(" ");
                p.sendMessage(ColorUtils.chat("&6&m----------------------------------------------------"));
                p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
            case ULTIMATE -> {
                String msg = String.format(
                    """
                  
                  
                   
                         [ %s ]
                         %s
                        축하합니다.
                   
                   
                   
                   
                   """, grade, gacha.getKey()
                );
                p.sendMessage(ColorUtils.chat(msg));
            }
        }
    }
}