package org.lazberry.xmaslegacy.plugin.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.user.SqlUserRepository;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

@Slf4j
@Registry
@Registry.Exclude(type = ServerType.LOBBY)
public class GlobalInitializer implements ServerInitializer {
	private final @NotNull UserManager um;
	private final @NotNull SqlUserRepository sr;
	private final @NotNull BagManager bm;

	@Inject
	public GlobalInitializer(@NotNull UserManager um, @NotNull SqlUserRepository sr, @NotNull BagManager bm) {
		this.um = um;
		this.sr = sr;
		this.bm = bm;
	}

	/**
	 * BungeeCord plugin messenger registered in this method.
	 * @param plugin Plugin instance
	 */
	@Override
	public void initiate(@NotNull XmasLegacy plugin) {
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungeecord:main");
		log.warn("This Christmas will be Perfect!");
	}

	@Override
	public void shutdown(@NotNull XmasLegacy plugin) {
		log.info("User info is automatically saved!");

		log.info("Bag data is automatically saved!");
		log.info("Stopping Hunting Zone spawning.");
	}
}
