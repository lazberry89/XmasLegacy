package xmaslegacy.Env;

import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.XmasLegacy;

import java.util.Map;

public enum ConsumableManager {
	INSTANCE;

    private @Nullable BukkitTask task;
    private @Getter boolean isRunning = false;
    private final @NotNull UserManager um;
	private final @NotNull BagManager bm;

    ConsumableManager() {
        this.um = UserManager.INSTANCE;
		this.bm = BagManager.INSTANCE;
    }

    public static ItemStack basicFood(int amount) {
		ItemStack item;
		var oraxen = OraxenItems.getItemById("cookie");
		if (oraxen == null) {item = new ItemStack(Material.COOKIE);} else {
		item = oraxen.build();}

        ItemStack a = ItemBuilder.of(XmasLegacy.getInstance(), item)
                .setName(ColorUtils.chat("&c&l라즈베리 쿠키"))
                .setLore(ColorUtils.chat("&8맛은 있는데,배는 고플걸"))
                .setGlint(true)
                .hideAllFlags()
                .addAttribute(Attribute.LUCK, 0.1, AttributeModifier.Operation.ADD_NUMBER)
                .build();
        a.setAmount(amount);
        return a.clone();
    }

    public void runCookieTimer(XmasLegacy plugin) {
        if (this.isRunning || this.task != null) return;
        this.isRunning = true;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                var user = um.getUser(p.getUniqueId());
                if (user == null) continue;

                if (!user.ifWantsCookie()) continue;
                Map<Integer, ItemStack> leftOver = p.getInventory().addItem(basicFood(Constants.COOKIE_COUNT));

				if (!leftOver.isEmpty()) {
					leftOver.values().forEach(item -> bm.addItem(p, item));
				}
            }
        }, 20 * 60 * Constants.COOKIE_TIMER_MINUTE, 20 * 60 * Constants.COOKIE_TIMER_MINUTE);
    }

    public void stopCookieTimer() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.isRunning = false;
    }

}
