package org.lazberry.xmaslegacy.blueprint;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.time.Duration;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class TaskResumeListener implements Listener {
    private final BluePrintManager bpm;

    @Inject
    public TaskResumeListener(BluePrintManager bpm) {
        this.bpm = bpm;
    }

    @EventHandler
    public void askPlayerToResumeBuildingTask(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        var option = ClickCallback.Options.builder()
                .lifetime(Duration.ofMinutes(1))
                .uses(1)
                .build();

        Component button = ColorUtils.chat("&a&l[재개하기]").clickEvent(ClickEvent.callback(audience -> {
            if (!(audience instanceof Player target)) return;
            if (bpm.hasStoppedBuildTask(target.getUniqueId())) {
                bpm.resumeBuilding(target, () ->
                    InfoUtils.info(target, "작업이 완료되었습니다. 확인해보세요!"));
            } else InfoUtils.error(target, "재개할 작업이 발견되지 않았습니다.");
        }, option));
        Component askComponent = ColorUtils.chat("진행하던 도면 건축 작업이 존재합니다. 다시 시작하시겠습니까? ");
        if (bpm.hasStoppedBuildTask(p.getUniqueId()))
            InfoUtils.warn(p, askComponent.append(button));
    }
}
