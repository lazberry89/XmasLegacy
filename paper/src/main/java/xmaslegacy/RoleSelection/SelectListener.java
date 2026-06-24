package xmaslegacy.RoleSelection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.XmasLegacy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Listeners
public class SelectListener implements Listener {
    private final RoleSelectInterface RSTI = new RoleSelectInterface();
    private final XmasLegacy plugin;
    private final UserManager UM;

    public SelectListener() {
        this.plugin = XmasLegacy.getInstance();
        this.UM = UserManager.INSTANCE;
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
        Role role = user.getRole();
        if (!BasicRoles.USER.equals(role)) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 직업을 선택했어요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            p.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
            return;
        }
        switch (slot) {
            case 2 -> openSelectionInv(p, BasicRoles.WARRIOR);
            case 4 -> openSelectionInv(p, BasicRoles.ROGUE);
            case 6 -> openSelectionInv(p, BasicRoles.MAGE);
            case 11 -> openSelectionInv(p, BasicRoles.KNIGHT);
            case 13 -> openSelectionInv(p, BasicRoles.ARCHER);
            case 15 -> openSelectionInv(p, BasicRoles.PRIEST);
            case 20 -> openSelectionInv(p, BasicRoles.MINER);
            case 22 -> openSelectionInv(p, BasicRoles.MERCHANT);
            case 24 -> openSelectionInv(p, BasicRoles.GATHERER);
            case 29 -> openSelectionInv(p, BasicRoles.FARMER);
            case 31 -> openSelectionInv(p, BasicRoles.CRAFTER);
            case 33 -> p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            default -> e.setCancelled(true);
        }
    }

    private void openSelectionInv(@NotNull Player p,@NotNull BasicRoles role) {
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
        BasicRoles select = holder.getSelectedRole();

        var user = UM.getUser(p.getUniqueId());
        if (user == null) {
            ClickCallback.Options options = ClickCallback.Options.builder()
                    .uses(1)
                    .lifetime(Duration.ofMinutes(3))
                    .build();
            Component reload = ColorUtils.chat("&c&l[ 정보 불러오기 ]").hoverEvent(HoverEvent.showText(ColorUtils.chat("&7유저정보를 다시 불러옵니다.")))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof Player t) {
                            CompletableFuture.supplyAsync(() -> UM.load(t.getUniqueId(), t.getName())).whenComplete((loadUser, throwable) -> {
                                if (throwable != null || loadUser == null) {
                                    p.sendMessage(ColorUtils.chat(Alert.RED + " 로드에 실패했어요! 관리자에게 문의해주세요. '/문의 ..'"));
                                    plugin.getSLF4JLogger().error("Error occurred while reloading User Info UUID -> {} ", t.getUniqueId(), throwable);
                                    return;
                                }
                                InfoUtils.info(t, "유저정보를 성공적으로 불러왔습니다!");
                            });
                        }
                    }, options));
            p.sendMessage(ColorUtils.chat(Alert.RED + " 유저정보를 불러오지 못했어요. 로드할까요? ").append(reload));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);

            return;
        }

        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            case 20 -> {
                if (UM.startRole(p.getUniqueId(), select)) {
                    p.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
                    p.sendMessage(ColorUtils.chat(String.format("%s 직업 &6&l%s&f를 선택하셨군요! 좋은 선택입니다.",  Alert.XmasLegacy, select)));
                    p.sendMessage(ColorUtils.chat(String.format("%s 안내인의 지시를 따라 이제 &6'%s'&f(으)로의 삶을 즐겨보세요!", Alert.YELLOW, select)));
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
