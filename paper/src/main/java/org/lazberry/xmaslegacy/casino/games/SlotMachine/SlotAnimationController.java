package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.utils.Axiom;

import java.util.concurrent.ThreadLocalRandom;

public class SlotAnimationController {
    private final XmasLegacy plugin;
    private final SlotMachine machine;
    private BlockDisplay display1, display2, display3;

    public SlotAnimationController(XmasLegacy plugin, SlotMachine machine) {
        this.plugin = plugin;
        this.machine = machine;
    }

    public void startSpin(Symbol finalSym1, Symbol finalSym2, Symbol finalSym3, Runnable onComplete) {
        display1 = spawnOrGet(display1, machine.getSlot1());
        display2 = spawnOrGet(display2, machine.getSlot2());
        display3 = spawnOrGet(display3, machine.getSlot3());

        spinReel(display1, finalSym1, 40, 1.0f);
        spinReel(display2, finalSym2, 60, 1.2f);

        int reel3Time = (finalSym1 == finalSym2) ? 100 : 80;
        spinReel(display3, finalSym3, reel3Time, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, onComplete, reel3Time + 5L);
    }

    private void spinReel(BlockDisplay display, Symbol finalSymbol, int stopTick, float soundPitch) {
        new BukkitRunnable() {
            int ticks = 0;
            final int SPIN_SPEED = 4;

            @Override
            public void run() {
                if (display == null || display.isDead()) {
                    cancel();
                    return;
                }

                if (ticks >= stopTick) {
                    display.setInterpolationDuration(0);
                    display.setBlock(finalSymbol.getShowMaterial().createBlockData());

                    Transformation trans = display.getTransformation();
                    trans.getTranslation().set(0, 0, 0); // 정중앙 고정
                    display.setTransformation(trans);

                    display.getWorld().playSound(display.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, soundPitch);
                    cancel();
                    return;
                }

                if (ticks % SPIN_SPEED == 0) {
                    display.setInterpolationDuration(0);
                    display.setInterpolationDelay(0);

                    Transformation resetTrans = display.getTransformation();
                    resetTrans.getTranslation().set(0, 0.8f, 0);
                    display.setTransformation(resetTrans);

                    Symbol[] symbols = Symbol.values();
                    Symbol randomSymbol = symbols[ThreadLocalRandom.current().nextInt(symbols.length)];
                    display.setBlock(randomSymbol.getShowMaterial().createBlockData());

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        display.setInterpolationDuration(SPIN_SPEED);
                        Transformation slideTrans = display.getTransformation();
                        slideTrans.getTranslation().set(0, -0.8f, 0);
                        display.setTransformation(slideTrans);
                    }, 1L);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private BlockDisplay spawnOrGet(BlockDisplay current, Location loc) {
        if (current != null && !current.isDead()) return current;

        Location center = Axiom.toExactCenterLocation(loc);
        BlockDisplay bd = (BlockDisplay) center.getWorld().spawnEntity(center, EntityType.BLOCK_DISPLAY);
        bd.setPersistent(false);
        bd.setBlock(Symbol.NO_LUCK.getShowMaterial().createBlockData());
        return bd;
    }
}