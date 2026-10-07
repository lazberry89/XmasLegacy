package org.lazberry.xmaslegacy.enchant.material.shop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class MaterialShopItemCirculator implements Tasks {
    private final EnchantShopManager shopManager;
    private volatile BukkitTask task;

    @Inject
    public MaterialShopItemCirculator(EnchantShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) synchronized (this) {
            if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                var shop = shopManager.getMaterialShop();
                var viewers = shop.getInventory().getViewers();
                if (viewers.isEmpty()) return;

                shop.circulateShowItem();
                viewers.stream()
                        .filter(Player.class::isInstance)
                        .map(Player.class::cast)
                        .forEach(Player::updateInventory);
            }, 0L, 20L);
        }
    }

    @Override
    public void stopTask() {
        if (task != null) synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }
}
