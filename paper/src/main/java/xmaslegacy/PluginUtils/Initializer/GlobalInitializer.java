package xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Env.ConsumableManager;
import xmaslegacy.Icing.IcingListener;
import xmaslegacy.Icing.IcingSystem;
import xmaslegacy.InquireTeleportCommand;
import xmaslegacy.InquiryCommandManager;
import xmaslegacy.LogCommands.LogCommand;
import xmaslegacy.PartyScoreBoard.UserPartyScoreBoard;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.RoleManagers.FirstRoleManager.Miner.SpecialOre;
import xmaslegacy.RuleCommands.RuleCommand;
import xmaslegacy.ServerJoinListener;
import xmaslegacy.ServerPrefix.ChatPrefixListener;
import xmaslegacy.XmasLegacy;

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
		//UserSaveManager.startTask(plugin);

		//RankingSystem.INSTANCE.startRankTask();

		//RankBoardSystem.INSTANCE.startBoardTask(plugin);

		//IcingSystem.INSTANCE.startTask(plugin);

		UserPartyScoreBoard.INSTANCE.startTask();
		ConsumableManager.INSTANCE.runCookieTimer(plugin);

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
		//UserSaveManager.stopTask();
		RegionManager.INSTANCE.saveAll();

		//RankingSystem.INSTANCE.stopRankTask();
		//RankBoardSystem.INSTANCE.stopBoardTask();

		//IcingSystem.INSTANCE.stopTask();

		UserPartyScoreBoard.INSTANCE.stopTask();

		UserManager.INSTANCE.getUsers().forEach(SqlUserRepository.INSTANCE::saveUser);
		log.info("User info is automatically saved!");

		ConsumableManager.INSTANCE.stopCookieTimer();
		BagManager.INSTANCE.saveAllBags();
		log.info("Bag data is automatically saved!");
		log.info("Stopping Hunting Zone spawning.");
	}
}
