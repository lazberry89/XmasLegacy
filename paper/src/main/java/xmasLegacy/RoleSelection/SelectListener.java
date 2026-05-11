package xmasLegacy.RoleSelection;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

public class SelectListener implements Listener {
    private final RoleSelectInterface RSTI = new RoleSelectInterface(JavaPlugin.getPlugin(XmasLegacy.class));
    private final XmasLegacy plugin;
    private final UserManager UM;

    public SelectListener(XmasLegacy plugin) {
        this.plugin = plugin;
        this.UM = plugin.UM;
    }

    @EventHandler
    public void onJobSelect(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof RoleSelectInterface)) return;

        int slot = e.getSlot();
        e.setCancelled(true);
        User user = UM.getUser(p.getUniqueId());
        if (user == null) {
            sendErrorLog(p);
            return;
        }
        Roles role = user.getRole();
        if (!Roles.USER.equals(role)) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 이미 직업을 선택했어요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            p.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
            return;
        }
        switch (slot) {
            case 2 -> openSelectionInv(p, Roles.WARRIOR);
            case 4 -> openSelectionInv(p, Roles.ROGUE);
            case 6 -> openSelectionInv(p, Roles.MAGE);
            case 11 -> openSelectionInv(p, Roles.KNIGHT);
            case 13 -> openSelectionInv(p, Roles.ARCHER);
            case 15 -> openSelectionInv(p, Roles.PRIEST);
            case 20 -> openSelectionInv(p, Roles.MINER);
            case 22 -> openSelectionInv(p, Roles.MERCHANT);
            case 24 -> openSelectionInv(p, Roles.GATHERER);
            case 29 -> openSelectionInv(p, Roles.FARMER);
            case 31 -> openSelectionInv(p, Roles.CRAFTER);
            case 33 -> p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            default -> e.setCancelled(true);
        }
    }

    private void openSelectionInv(@NotNull Player p,@NotNull Roles role) {
        RoleSelectionInterface RSI = new RoleSelectionInterface(role);
        p.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
        p.openInventory(RSI.getInventory());
        p.updateInventory();
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
    }

    @EventHandler
    public void FinalSelection(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof RoleSelectionInterface holder)) return;
        Roles select = holder.getRole();

        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            case 20 -> {
                if (UM.startRole(p.getUniqueId(), select)) {
                    p.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
                    p.sendMessage(ColorUtils.chat(String.format("%s 직업 &6&l%s&f를 선택하셨군요! 좋은 선택입니다.",  Prefix.XmasLegacy, select)));
                    p.sendMessage(ColorUtils.chat(String.format("%s 안내인의 지시를 따라 이제 &6'%s'&f(으)로의 삶을 즐겨보세요!", Prefix.YELLOW, select)));
                    p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
            }
            case 24 -> {
                p.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
                p.openInventory(RSTI.getInventory());
                p.updateInventory();
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            }
        }
    }

    private void sendErrorLog(Player p) {
        plugin.getSLF4JLogger().error("Could not find User while selecting Roles : {}, {}", p.getName(), p.getUniqueId());
    }

    @EventHandler
    public void QuitSelecting(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof RoleSelectInterface) &&
                !(e.getInventory().getHolder() instanceof RoleSelectionInterface)) return;
        if (InventoryCloseEvent.Reason.OPEN_NEW.equals(e.getReason())
                || InventoryCloseEvent.Reason.CANT_USE.equals(e.getReason())) return;
        Player p = (Player) e.getPlayer();

        p.sendMessage(ColorUtils.chat("&6나중에 다시 고민해봐요..!"));
        p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }
}
