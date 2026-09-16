package org.lazberry.xmaslegacy.casino.games.CoinFlip;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Registry.Include(type = ServerType.MAIN)
public class CoinFlipManager {
    private final Map<String, CoinFlip> machineMap = new ConcurrentHashMap<>();
    private final XmasLegacy plugin;

    @Inject
    public CoinFlipManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public CoinFlip registerMachine(String id, Location location, ItemStack coinItem) {
        return machineMap.computeIfAbsent(id, i -> new CoinFlip(i, location, coinItem));
    }

    public void removeMachine(String id) {
        machineMap.remove(id);
    }

    public void flip(Player player, String id, CoinFlip.State selection, int injectedCoin) {
        if (player == null || !player.isValid()) return;

        CoinFlip machine = machineMap.get(id);
        if (machine == null) {
            InfoUtils.error(player, "유효하지 않은 기계입니다.");
            return;
        }
        if (machine.getResult() == CoinFlip.State.ROLLING) {
            InfoUtils.error(player, "이미 동전 던지기가 진행 중입니다.");
            return;
        }
        if (selection == CoinFlip.State.ROLLING) {
            InfoUtils.error(player, "올바르지 않은 선택입니다.");
            return;
        }
        if (injectedCoin <= 0) {
            InfoUtils.error(player, "카지노 코인을 넣어주세요!");
            return;
        }
        machine.setResult(CoinFlip.State.ROLLING);
        CoinFlip.State finalResult = result(machine, selection);
        startCoinFlipAnimation(player, machine, selection, finalResult, injectedCoin);
    }

    private void startCoinFlipAnimation(Player player, CoinFlip machine, CoinFlip.State selection, CoinFlip.State finalResult, int injectedCoin) {
        Location spawnLoc = machine.getLocation().clone().add(0, 1.2, 0);
        ItemDisplay display = machine.spawnDisplay(spawnLoc);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30;

            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    display.remove();
                    machine.setResult(finalResult);

                    boolean isWin = (selection == finalResult);
                    if (isWin) {
                        int amount = injectedCoin * 2;
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                        Casino.sendIconAlert(player, "동전 던지기 승리! 코인&6 " + amount + "&f개를 획득하셨습니다.");
                        Casino.giveCoin(player, amount);
                    } else {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
                        InfoUtils.error(player, "아쉽게도 패배하셨습니다...");
                    }

                    cancel();
                    return;
                }

                float yOffset = (float) Math.sin((double) ticks / maxTicks * Math.PI) * 0.5f;

                Quaternionf leftRotation = new Quaternionf().rotateX((float) Math.toRadians(ticks * 24));
                Quaternionf rightRotation = new Quaternionf();
                Vector3f translation = new Vector3f(0, yOffset, 0);
                Vector3f scale = display.getTransformation().getScale();

                display.setTransformation(new org.bukkit.util.Transformation(
                        translation,
                        leftRotation,
                        scale,
                        rightRotation
                ));

                player.playSound(spawnLoc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.2f + (ticks * 0.02f));
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public CoinFlip.State result(CoinFlip machine, CoinFlip.State userSelection) {
        if (userSelection == CoinFlip.State.ROLLING) return CoinFlip.State.ROLLING;

        boolean win = ThreadLocalRandom.current().nextDouble() < machine.getChanceUserWin();
        return win ? userSelection : userSelection.reverse();
    }
}
