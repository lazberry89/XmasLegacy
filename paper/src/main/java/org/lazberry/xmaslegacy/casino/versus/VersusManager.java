package org.lazberry.xmaslegacy.casino.versus;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.bet.BetInterface;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

import java.util.Optional;

@Registry.Include(type = ServerType.MAIN)
public class VersusManager {
    private final XmasLegacy plugin;
    private volatile VersusField field;
    private @Getter @Setter boolean canBet = false;

    @Inject
    public VersusManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public Optional<VersusField> getField() {
        return Optional.ofNullable(field);
    }

    public void registerField(Location blue, Location red, Location blueRoom, Location redRoom, boolean force) {
        if (field == null || force) {
            synchronized (this) {
                if (field == null || force) {
                    field = new VersusField(blue, red, blueRoom, redRoom, plugin);
                }
            }
        }
    }

    public void removeField() {
        synchronized (this) {
            if (field != null) {
                field = null;
            }
        }
    }

    public void join(@Nullable Player player) {
        if (player == null || !player.isValid()) return;

        var optional = getField();
        if (optional.isEmpty()) {
            InfoUtils.error(player, "필드가 설정되지 않았습니다.");
            return;
        }

        var f = optional.get();
        if (f.isRunning()) {
            InfoUtils.error(player, "이미 경기가 시작되었습니다!");
            return;
        }
        synchronized (f) {
            var uuid = player.getUniqueId();
            if (f.getBlueFighter().equals(uuid) || f.getRedFighter().equals(uuid)) {
                InfoUtils.warn(player, "이미 경기에 참가하였습니다.");
                return;
            }
            if (f.isFull()) {
                InfoUtils.error(player, "이미 모든 참가자가 배정되었습니다.");
                return;
            }
            f.joinRandomly(uuid);
            InfoUtils.info(player, "게임에 참가했습니다. 시작 시 자동으로 이동됩니다.");
            GlowUtils.glow(player, NamedTextColor.GOLD);

            if (f.isFull()) {
            }
        }
    }

    private void startBetting() {
        synchronized (this) {
            OptionalUtils.ifNotNullOrElse(field, f -> {
                setCanBet(true);
                f.setRunning(false);
                Bukkit.broadcast(betComponent(f));
            }, () -> {

            });
        }
    }

    private Component betComponent(VersusField f) {
        if (!f.isFull()) return ColorUtils.chat("");

        Player blue = Bukkit.getPlayer(f.getBlueFighter());
        Player red = Bukkit.getPlayer(f.getRedFighter());
        if (blue == null || red == null) return ColorUtils.chat("");

        var clickable = ColorUtils.chat("&6&l[베팅하기]").clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p) {
                        p.openInventory(new BetInterface(blue, red, plugin).getInventory());
                    }
                }))
                .hoverEvent(HoverEvent.showText(ColorUtils.chat("&7클릭하여 베팅화면을 로드합니다.")));
        return Casino.icon.append(ColorUtils.chat(" 곧 1대1 게임이 시작됩니다. 카지노의 인원은 베팅이 가능합니다. "))
                .append(clickable);
    }

    public void leave(Player player) {
        if (player == null || !player.isValid()) return;
        var uuid = player.getUniqueId();

        var optional = getField();
        if (optional.isEmpty()) {
            InfoUtils.error(player, "게임에 참여하지 않았습니다.");
            return;
        }
        var f = optional.get();
        if (!f.isFighter(uuid)) {
            InfoUtils.error(player, "게임에 참여하지 않았습니다.");
            return;
        }
        if (f.isRunning()) {
            InfoUtils.error(player, "경기중에는 퇴장할 수 없습니다!");
            return;
        }
        if (f.leave(uuid)) InfoUtils.info(player, "퇴장하였습니다.");
        else InfoUtils.error(player, "퇴장할 수 없습니다.");
    }
}
