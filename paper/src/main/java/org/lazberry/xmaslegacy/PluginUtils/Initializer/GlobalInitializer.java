package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Farmer.AgeableCrops;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Miner.SpecialOre;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

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

		if (AgeableCrops.RegisterRecipe()) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		if (SpecialOre.RegisterRecipe()) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		log.info("XmasLegacy Plugin Enabled!");
		log.warn("This Christmas will be Perfect!");
	}

	@Override
	public void shutdown(@NotNull XmasLegacy plugin) {
		um.getUsers().forEach(sr::saveUser);
		log.info("User info is automatically saved!");

		bm.saveAllBags();
		log.info("Bag data is automatically saved!");
		log.info("Stopping Hunting Zone spawning.");
	}
}
