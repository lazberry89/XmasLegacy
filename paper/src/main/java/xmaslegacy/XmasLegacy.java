package xmaslegacy;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Env.ConsumableManager;
import xmaslegacy.HuntingZone.MobSpawnManager;
import xmaslegacy.Icing.IcingListener;
import xmaslegacy.Icing.IcingSystem;
import xmaslegacy.LogCommands.LogCommand;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.PluginUtils.ReflectionManager;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.RoleSelection.RoleViewDesign;
import xmaslegacy.RuleCommands.RuleCommand;
import xmaslegacy.ServerPrefix.ChatPrefixListener;

import java.io.IOException;

@Slf4j
public final class XmasLegacy extends JavaPlugin {

	@Getter
	private static XmasLegacy instance;

	public XmasLegacy() {
		instance = this;
	}

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungeecord:main");
		UserManager.INSTANCE.initDataFolder(this.getDataFolder());

		//빙결시스템 시작
		registerIcingSystem();

		ServerInitializer.initiate(this);

		if (AgeableCrops.RegisterRecipe(this)) log.info("Recipe Registered!");
		else log.error("Recipe Not Registered!");

		getServer().getPluginManager().registerEvents(new ServerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new ChatPrefixListener(), this);

		registerGlobalCommand();

		log.info("XmasLegacy Plugin Enabled!");
		log.warn("This Christmas will be Perfect!");
	}

	private void registerIcingSystem() {
		IcingSystem.INSTANCE.startTask(this);
		getServer().getPluginManager().registerEvents(new IcingListener(), this);
	}

	private void registerGlobalCommand() {
		var inquiry = getCommand("문의");
		var move = getCommand("이동문의");
		var filter = getCommand("filter");
		var log = getCommand("log");
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
	public void onDisable() {
		RegionManager.INSTANCE.saveAll();

		UserManager.INSTANCE.getAllUsers().forEach(SqlUserRepository.INSTANCE::saveUser);
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

	public void registerReflection() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());
			ReflectionManager.registerListeners(classPath);
			ReflectionManager.registerCommands(classPath);
			ReflectionManager.registerAllRoles(classPath);
		} catch (IOException e) {
			log.error("Error occurred while registering instances! Disabling plugin.", e);
			this.getServer().broadcast(ColorUtils.chat(Alert.RED + " Failed to load main plugin System. Disabling plugin."));
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}
}