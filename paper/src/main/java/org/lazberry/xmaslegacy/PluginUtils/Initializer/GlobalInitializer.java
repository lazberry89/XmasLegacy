package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Icing.IcingListener;
import org.lazberry.xmaslegacy.InquireTeleportCommand;
import org.lazberry.xmaslegacy.InquiryCommandManager;
import org.lazberry.xmaslegacy.LogCommands.LogCommand;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Miner.SpecialOre;
import org.lazberry.xmaslegacy.RuleCommands.RuleCommand;
import org.lazberry.xmaslegacy.ServerJoinListener;
import org.lazberry.xmaslegacy.ServerPrefix.ChatPrefixListener;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.XmasLegacy;

@Slf4j
public class GlobalInitializer implements ServerInitializer {

	/**
	 * BungeeCord plugin messenger registered in this method.
	 * @param plugin Plugin instance
	 */
	@Override
	public void enable(@NotNull XmasLegacy plugin) {
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungeecord:main");
		UserManager.INSTANCE.initDataFolder(plugin.getDataFolder());
		plugin.registerReflection();

		if (AgeableCrops.RegisterRecipe(plugin)) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		if (SpecialOre.RegisterRecipe()) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		plugin.getServer().getPluginManager().registerEvents(new ServerJoinListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new ChatPrefixListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new IcingListener(), plugin);

		registerGlobalCommand(plugin);

		log.info("XmasLegacy Plugin Enabled!");
		log.warn("This Christmas will be Perfect!");
	}

	private void registerGlobalCommand(@NotNull XmasLegacy plugin) {
		var inquiry = plugin.getCommand("문의");
		var move = plugin.getCommand("이동문의");
		var filter = plugin.getCommand("filter");
		var log = plugin.getCommand("log");
		if (inquiry != null) inquiry.setExecutor(new InquiryCommandManager());
		if (move != null) move.setExecutor(new InquireTeleportCommand());
		var rule = new RuleCommand();
		if (filter != null) {
			filter.setExecutor(rule);
			filter.setTabCompleter(rule);
		}
		var logCommand = new LogCommand();
		if (log != null) {
			log.setExecutor(logCommand);
			log.setTabCompleter(logCommand);
		}
	}

	@Override
	public void disable(@NotNull XmasLegacy plugin) {
		plugin.unregisterReflection();
		RegionManager.INSTANCE.saveAll();

		UserManager.INSTANCE.getUsers().forEach(SqlUserRepository.INSTANCE::saveUser);
		log.info("User info is automatically saved!");

		BagManager.INSTANCE.saveAllBags();
		log.info("Bag data is automatically saved!");
		log.info("Stopping Hunting Zone spawning.");
	}
}
