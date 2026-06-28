package xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Env.ConsumableManager;
import xmaslegacy.HuntingZone.MobSpawnManager;
import xmaslegacy.Icing.IcingListener;
import xmaslegacy.Icing.IcingSystem;
import xmaslegacy.*;
import xmaslegacy.LogCommands.LogCommand;
import xmaslegacy.PartyScoreBoard.UserPartyScoreBoard;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.Ranks.RankBoardSystem;
import xmaslegacy.Ranks.RankingSystem;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.RoleManagers.FirstRoleManager.Miner.SpecialOre;
import xmaslegacy.RoleSelection.RoleViewDesign;
import xmaslegacy.RuleCommands.RuleCommand;
import xmaslegacy.ServerPrefix.ChatPrefixListener;

@Slf4j
public class GlobalInitializer implements ServerInitializer {

	@Override
	public void enable(@NotNull XmasLegacy plugin) {
		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungeecord:main");
		UserManager.INSTANCE.initDataFolder(plugin.getDataFolder());

		RankingSystem.INSTANCE.startRankTask();

		RankBoardSystem.INSTANCE.resetBoards();
		RankBoardSystem.INSTANCE.startBoardTask(plugin);

		registerIcingSystem(plugin);

		ServerInitializer.initiate(plugin);

		if (AgeableCrops.RegisterRecipe(plugin)) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		if (SpecialOre.RegisterRecipe()) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		plugin.getServer().getPluginManager().registerEvents(new ServerJoinListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new ChatPrefixListener(), plugin);

		registerGlobalCommand(plugin);

		log.info("XmasLegacy Plugin Enabled!");
		log.warn("This Christmas will be Perfect!");
	}

	private void registerIcingSystem(@NotNull XmasLegacy plugin) {
		IcingSystem.INSTANCE.startTask(plugin);
		plugin.getServer().getPluginManager().registerEvents(new IcingListener(), plugin);
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
		UserSaveManager.stopTask();
		RegionManager.INSTANCE.saveAll();

		RankingSystem.INSTANCE.stopRankTask();
		RankBoardSystem.INSTANCE.stopBoardTask();
		RankBoardSystem.INSTANCE.resetBoards();

		UserPartyScoreBoard.INSTANCE.stopTask();
		UserManager.INSTANCE.getUsers().forEach(SqlUserRepository.INSTANCE::saveUser);
		log.info("User info is automatically saved!");

		IcingSystem.INSTANCE.stopTask();

		ConsumableManager.INSTANCE.stopCookieTimer();
		RoleViewDesign.INSTANCE.stopVisualLoop();
		BagManager.INSTANCE.saveAllBags();
		log.info("Bag data is automatically saved!");

		log.info("Stopping Hunting Zone spawning.");
		MobSpawnManager.INSTANCE.stopTask();

		//UserTagManager.stopTask();
	}
}
