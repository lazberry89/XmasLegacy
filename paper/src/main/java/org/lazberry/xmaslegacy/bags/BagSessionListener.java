package org.lazberry.xmaslegacy.bags;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.database.BagSaveManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.StunUtils;

@Slf4j
@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class BagSessionListener implements Listener {
    private final BagManager bagManager;
    private final BagSaveManager saveManager;
    private final XmasLegacy plugin;

    @Inject
    public BagSessionListener(BagManager bagManager, BagSaveManager saveManager, XmasLegacy plugin) {
        this.bagManager = bagManager;
        this.saveManager = saveManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void loadBagWhenPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var uuid = player.getUniqueId();

        StunUtils.stun(uuid, "정보 로드");
        saveManager.loadBag(uuid).whenComplete((bag, ex) ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                StunUtils.release(uuid);
                if (ex != null) {
                    InfoUtils.error(player, "가방 정보를 불러오지 못했습니다. 관리자에게 문의하세요.");
                    log.error("Failed to load player({})'s bag contents.", player.getName());
                    return;
                }
                bagManager.setUserBag(uuid, bag);
            }));
    }

    @EventHandler
    public void saveBagWhenPlayerLeave(PlayerQuitEvent e) {
        var player = e.getPlayer();
        var uuid = player.getUniqueId();

        saveManager.saveBag(bagManager.getBag(uuid)).whenComplete((v, ex) ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (ex != null) {
                    log.error("Failed to save player({})'s bag on quit. Data may be lost!", player.getName(), ex);
                } else {
                    log.info("Saved player({})'s bag contents.", player.getName());
                }
                // 플레이어가 이미 서버를 떠난 상태이므로 저장 성공 여부와 관계없이 메모리에서는 제거
                bagManager.removeUserBag(uuid);
            }));
    }
}
