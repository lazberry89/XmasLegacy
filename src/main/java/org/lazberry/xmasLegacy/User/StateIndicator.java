package org.lazberry.xmasLegacy.User;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;
import org.lazberry.xmasLegacy.XmasLegacy;

public class StateIndicator {
    private final UserManager UM;
    private final XmasLegacy plugin;
    private BukkitTask task;

    public StateIndicator(XmasLegacy plugin, UserManager um) {
        this.UM = um;
        this.plugin = plugin;
        runStateInfo();
    }

    private void sendUserState(Player p) {
        User user = UM.getUser(p);
        if (user == null) return;
        double health = p.getHealth();
        AttributeInstance mh = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (mh == null) return;
        double maxHealth = mh.getValue();
        int energy = p.getFoodLevel();
        int dollar = user.getDollars();

        String actionBarText = String.format("[ 체력 %d/%d | 에너지 %d/20 | 자본 %d$ ]",
                (int) health, (int) maxHealth, energy, dollar);

        p.sendActionBar(ComponentChanger.comp(actionBarText));
    }

    private void runStateInfo() {
        if (task != null) return;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    sendUserState(p);
                }
            }
        }.runTaskTimer(plugin, 5, 5);
    }

    public void stopTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }
}
