package org.lazberry.xmasLegacy.Env;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmasLegacy.PlayerUtils.BagManager;
import org.lazberry.xmasLegacy.UserManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.Map;

public class ConsumableManager implements Listener {
    private final XmasLegacy plugin;
    private BukkitTask task;
    private boolean isRunning = false;
    private final UserManager UM;
	private final BagManager BM;

    public ConsumableManager(XmasLegacy plugin, UserManager UM, BagManager BM) {
        this.plugin = plugin;
        this.UM = UM;
		this.BM = BM;
    }

    public static ItemStack basicFood(int amount) {
        ItemStack a =  ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.POTATO)
                .setName(ColorUtils.chat("&4&l라즈베리 쿠키"))
                .setLore(ColorUtils.chat("&8맛은 있는데,배는 고플걸"))
                .setGlint(true)
                .hideAllFlags()
                .addAttribute(Attribute.LUCK, 0.1, AttributeModifier.Operation.ADD_NUMBER)
                .build();
        a.setAmount(amount);
        return a.clone();
    }

    @EventHandler
    public void joinPrize(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.getInventory().addItem(basicFood(16));
    }

    public void runCookieTimer(XmasLegacy plugin) {
        if (this.isRunning || this.task != null) return;
        this.isRunning = true;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!UM.getUser(p).ifWantsCookie()) continue;
                Map<Integer, ItemStack> leftOver = p.getInventory().addItem(basicFood(16));

				if (!leftOver.isEmpty()) {
					leftOver.values().forEach(item -> BM.addItem(p, item, item.getAmount()));
				}
            }
        }, 20 * 60 * 60L, 20 * 60 * 60L);
    }

    public void stopCookieTimer() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
