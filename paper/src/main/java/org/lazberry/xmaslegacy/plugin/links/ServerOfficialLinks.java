package org.lazberry.xmaslegacy.plugin.links;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.ServerLinks;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings("UnstableApiUsage")
@Registry.Include(type = ServerType.GLOBAL)
public class ServerOfficialLinks implements Initiator {
    private final Map<ServerLinks.Type, URI> serverLinkValues = new ConcurrentHashMap<>();
    private final ServerLinkConfig serverLinkConfig;
    private final XmasLegacy plugin;
    private ServerLinks serverLinks;

    @Inject
    public ServerOfficialLinks(ServerLinkConfig serverLinkConfig, XmasLegacy plugin) {
        this.serverLinkConfig = serverLinkConfig;
        this.plugin = plugin;
    }

    @Override
    public void init() {
		clearLinks();
        serverLinkValues.clear();
        serverLinkValues.putAll(serverLinkConfig.loadSync());
        serverLinks = createLinks();
    }

    private ServerLinks createLinks() {
        ServerLinks links = Bukkit.getServerLinks();

        serverLinkValues.forEach((links::addLink));
        return links;
    }

    public void reload() {
        serverLinkConfig.saveAsync(serverLinkValues).whenComplete((v, e) -> {
           if (e != null) {
               log.error("Failed to save server links file.", e);
               return;
           }
           serverLinkValues.clear();
           serverLinkConfig.loadAsync().whenComplete((value, ex) -> {
               if (ex != null) {
                   log.error("Failed to load server links file.", ex);
                   return;
               }
               serverLinkValues.putAll(value);
			   clearLinks();
               serverLinks = createLinks();
               Bukkit.getScheduler().runTask(plugin, this::updateAllUsers);
           });
        });
    }

    public void sendOfficialLinks(Player player) {
        player.sendLinks(serverLinks);
        InfoUtils.info(player, "서버 공식 링크가 업데이트 되었습니다. esc를 눌러 확인하세요!");
    }

    public void updateAllUsers() {
        Bukkit.getOnlinePlayers().forEach(this::sendOfficialLinks);
    }

	private void clearLinks() {
		if (serverLinks != null) serverLinks.getLinks().forEach(serverLink ->
				serverLinks.removeLink(serverLink));
		serverLinks = null;
	}

    @Override
    public void close() {
        serverLinkConfig.saveSync(serverLinkValues);
        serverLinkValues.clear();
		clearLinks();
    }
}
