package org.lazberry.xmaslegacy.casino.versus;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.bet.VersusBetInterface;
import org.lazberry.xmaslegacy.casino.versus.bet.VersusBetManager;
import org.lazberry.xmaslegacy.casino.versus.event.VersusResetEvent;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.*;

import java.util.Optional;

@Data
@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class VersusManager {
    private final XmasLegacy plugin;
    private volatile VersusField field;

    private boolean canBet = false;
    private int betAvailableSeconds = 10;
    private int beforeFightSeconds = 3;

    @Inject
    public VersusManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public Optional<VersusField> getField() {
        return Optional.ofNullable(field);
    }

    public void registerField(Location blue, Location red, Location blueRoom, Location redRoom, Location entrance, boolean force) {
        if (field == null || force) {
            synchronized (this) {
                if (field == null || force) {
                    field = new VersusField(blue, red, blueRoom, redRoom, entrance, plugin);
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
                if (f.teleportWaitingRoom()) {
                    startBetting();
                } else reset(GameResult.ERROR);
            } else reset(GameResult.LEAVE);
        }
    }

    private void startBetting() {
        synchronized (this) {
            OptionalUtils.ifNotNullOrElse(field, f -> {
                setCanBet(true);
                f.setRunning(false);
                Bukkit.broadcast(betComponent(f));
                betTimer(betAvailableSeconds);
            }, () -> {
                setCanBet(false);
                log.error("Game tried to start game, but field is not set.");
            });
        }
    }

    private void betTimer(int seconds) {
        synchronized (this) {
            OptionalUtils.ifNotNullOrElse(field, f ->
                new AtomicTimer(seconds, a -> {
                    if (!f.isFull()) {
                        log.error("Betting timer started cause fighter left.");
                        reset(GameResult.LEAVE);
                        a.stop();
                        return;
                    }
                    int current = a.getRemainingSeconds();
                    Player p1 = Bukkit.getPlayer(f.getRedFighter());
                    Player p2 = Bukkit.getPlayer(f.getBlueFighter());

                    if (p1 == null || p2 == null) {
                        log.error("Betting timer started cause fighter left.");
                        a.stop();
                        reset(GameResult.LEAVE);
                        return;
                    }
                    var title = TitleUtil.create(titleMaker(current), "");
                    p1.showTitle(title);
                    p2.showTitle(title);
                    p1.playSound(p1, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
                    p2.playSound(p2, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
                }, () -> {
                    Player p1 = Bukkit.getPlayer(f.getRedFighter());
                    Player p2 = Bukkit.getPlayer(f.getBlueFighter());

                    if (p1 == null || p2 == null) {
                        reset(GameResult.LEAVE);
                        return;
                    }

                    var betOver = TitleUtil.create("&6&l베팅종료", "");
                    var beforeStart = TitleUtil.create("&7&l게임 시작까지: ", "");
                    p1.showTitle(betOver);
                    p2.showTitle(betOver);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        p1.showTitle(beforeStart);
                        p2.showTitle(beforeStart);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> startBeforeGameTimer(beforeFightSeconds), 20L);
                    }, 30L);
                }).start()
            , () -> {
                reset(GameResult.ERROR);
                log.error("Failed to start timer.");
            });
        }
    }

    private void startBeforeGameTimer(int seconds) {
        synchronized (this) {
            OptionalUtils.ifNotNullOrElse(field, f ->
                new AtomicTimer(seconds, a ->
                        OptionalUtils.allNotNullOrElse(Bukkit.getPlayer(f.getRedFighter()), Bukkit.getPlayer(f.getBlueFighter()), (p1, p2) -> {
                            int current = a.getRemainingSeconds();
                            var secTitle = TitleUtil.create(beforeFightTitleMaker(current), "");
                            p1.showTitle(secTitle);
                            p2.showTitle(secTitle);

                            p1.playSound(p1, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                            p2.playSound(p2, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }, () -> {
                            a.stop();
                            reset(GameResult.LEAVE);
                        }), this::startGame).start()
            , () -> {
                reset(GameResult.ERROR);
                log.error("Failed to start main delayed Timer.");
            });
        }
    }

    public void startGame() {
        setCanBet(false);
        synchronized (this) {
            OptionalUtils.ifNotNullOrElse(field, f -> {
                f.setRunning(true);
                var blueUuid = f.getBlueFighter();
                var redUuid = f.getRedFighter();
                if (blueUuid == null || redUuid == null) {
                    reset(GameResult.LEAVE);
                    return;
                }

                OptionalUtils.allNotNullOrElse(Bukkit.getPlayer(blueUuid), Bukkit.getPlayer(redUuid), (blue, red) -> {
                    blue.teleport(f.getBlueSpawn());
                    red.teleport(f.getRedSpawn());
                    f.breakSequentially();
                }, () -> {
                    reset(GameResult.LEAVE);
                    log.error("Player instances are not found.");
                });
            }, () -> log.error("Failed to start game. Field is null!"));
        }
    }

    private String beforeFightTitleMaker(int times) {
        return switch (times) {
            case 3 -> "&e&l3";
            case 2 -> "&6&l2";
            case 1 -> "&c&l1";
            default -> "&7&l" + times;
        };
    }

    private String titleMaker(int time) {
        if (time <= 3) return "&c&l" + time;
        else if (time <= 7) return "&6&l" + time;
        else return "&e&l" + time;
    }

    private Component betComponent(VersusField f) {
        if (!f.isFull()) return ColorUtils.chat("");

        Player blue = Bukkit.getPlayer(f.getBlueFighter());
        Player red = Bukkit.getPlayer(f.getRedFighter());
        if (blue == null || red == null) return ColorUtils.chat("");

        var clickable = ColorUtils.chat("&6&l[베팅하기]").clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p) {
                        if (isCanBet())
                            p.openInventory(new VersusBetInterface(blue, red, plugin).getInventory());
                        else InfoUtils.error(p, "베팅 세션이 종료되었습니다.");
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
        if (f.leave(uuid)) {
            InfoUtils.info(player, "퇴장하였습니다.");
            GlowUtils.clearGlow(player);
        }
        else InfoUtils.error(player, "퇴장할 수 없습니다.");
    }

    public void reset(GameResult result) {
        synchronized (this) {
            OptionalUtils.ifNotNull(field, f -> {
                f.setRunning(false);
                var blueUuid = f.getBlueFighter();
                var redUuid = f.getRedFighter();
                Bukkit.getPluginManager().callEvent(new VersusResetEvent(blueUuid, redUuid, result));

                if (blueUuid != null) {
                    OptionalUtils.ifNotNull(Bukkit.getPlayer(blueUuid), p -> {
                        p.teleport(f.getEntrance());
                        GlowUtils.clearGlow(p);
                    });
                }
                if (redUuid != null) {
                    OptionalUtils.ifNotNull(Bukkit.getPlayer(redUuid), p -> {
                        p.teleport(f.getEntrance());
                        GlowUtils.clearGlow(p);
                    });
                }

                f.setBlueFighter(null);
                f.setRedFighter(null);
                f.restoreFences();
            });
        }
    }
}
