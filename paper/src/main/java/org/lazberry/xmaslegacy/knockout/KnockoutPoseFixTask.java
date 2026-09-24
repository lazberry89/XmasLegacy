package org.lazberry.xmaslegacy.knockout;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Objects;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class KnockoutPoseFixTask implements Tasks {
    private final Particle.DustTransition trans;
    private final KnockoutManager km;
    private volatile BukkitTask task;

    @Inject
    public KnockoutPoseFixTask(KnockoutManager km) {
        this.trans = new Particle.DustTransition(Color.RED, Color.BLACK, 0.4f);
        this.km = km;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) synchronized (this) {
            if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                km.getKnockoutPlayerId().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(Player::isValid)
                        .forEach(p -> {
                            p.setPose(Pose.SWIMMING, true);
                            p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                                    p.getLocation(), 5, 0.3, 0.3, 0.3, 0.01, trans);
                        });
            }, 0L, 10L);
        }
    }

    @Override
    public void stopTask() {

    }
}
