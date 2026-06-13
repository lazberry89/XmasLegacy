package xmasLegacy;

import com.google.common.reflect.ClassPath;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserRepository;
import xmasLegacy.Env.ConsumableManager;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.FirstRoleManager.FirstRoleManager;
import xmasLegacy.FirstRoleManager.Merchant.MerchantStockInterface;
import xmasLegacy.HuntingZone.CustomMobs.MobRepository;
import xmasLegacy.HuntingZone.HuntingZoneManager;
import xmasLegacy.HuntingZone.MobSpawnManager;
import xmasLegacy.Lobby.LobbyCommand;
import xmasLegacy.Lobby.LobbyListener;
import xmasLegacy.Lobby.LobbyManager;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.RoleSelection.RoleViewDesign;
import xmasLegacy.SecondaryRoleManager.SecondRoleManager;
import xmasLegacy.ServerPrefix.ChatPrefixListener;
import xmasLegacy.ServerPrefix.UserTagManager;
import xmasLegacy.TransferPortal.PortalManager;

@SuppressWarnings("FieldCanBeLocal, DataFlowIssue")
public final class XmasLegacy extends JavaPlugin {
	private static XmasLegacy instance;

	public static XmasLegacy getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungeecord:main");
		instance = this;

		UserManager.INSTANCE.initDataFolder(this.getDataFolder());

		if (AgeableCrops.RegisterRecipe()) {
			getSLF4JLogger().info("Recipe Registered!");
		} else {
			getSLF4JLogger().error("Recipe Not Registered!");
		}

		getServer().getPluginManager().registerEvents(new ServerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new ChatPrefixListener(), this);

		getCommand("문의").setExecutor(new InquiryCommandManager());
		getCommand("이동문의").setExecutor(new InquireTeleportCommand());
		var rule = new RuleCommand();
		getCommand("filter").setExecutor(rule);
		getCommand("filter").setTabCompleter(rule);
		var log = new LogCommand();
		getCommand("log").setExecutor(log);
		getCommand("log").setTabCompleter(log);
		serverType();

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
	}

	@Override
	public void onDisable() {
		RegionManager.INSTANCE.saveAll();

		UserRepository repository = new SqlUserRepository();
		UserManager.INSTANCE.getAllUsers().forEach(repository::saveUser);
		getSLF4JLogger().info("모든 유저 데이터를 자동 저장했습니다.");


		ConsumableManager.INSTANCE.stopCookieTimer();

		BagManager.INSTANCE.saveAllBags();
		getSLF4JLogger().info("모든 가방 데이터를 자동 저장했습니다.");

		getSLF4JLogger().info("사냥터 몹 스폰을 종료합니다.");
		MobSpawnManager.INSTANCE.stopTask();

		UserTagManager.stopTask();
	}

	private void serverType() {
		saveDefaultConfig();
		String serverType = getConfig().getString("server-type", ServerType.MAIN.str());

		// ------------------ [LOBBY MODE] ------------------
		if (serverType.equals(ServerType.LOBBY.str())) {
			getLogger().warning("Lobby 모드로 시작합니다.");
			var lobbyManager = new LobbyManager();

			getServer().getPluginManager().registerEvents(new LobbyListener(lobbyManager), this);

			var lobbyCommand = new LobbyCommand(lobbyManager);
			getCommand("lobby").setExecutor(lobbyCommand);
			getCommand("lobby").setTabCompleter(lobbyCommand);

			getSLF4JLogger().warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);

			// ------------------ [MAIN GAME MODE] ------------------
		} else if (serverType.equals(ServerType.MAIN.str())) {
			getLogger().warning("Main 모드로 시작합니다.");
			getSLF4JLogger().warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);
			RoleViewDesign.getInstance().init();

			RegionManager.INSTANCE.startGlobalIndicatorTask();

			MerchantStockInterface.getInstance();
			FirstRoleManager.INSTANCE.init();

			// SecondaryRole 초기화
			SecondRoleManager.INSTANCE.init();

			ConsumableManager.INSTANCE.runCookieTimer(this);
			BagManager.INSTANCE.loadAllBags();

			MobRepository.INSTANCE.init();

			HuntingZoneManager.INSTANCE.init();
			MobSpawnManager.INSTANCE.startTask();
			PortalManager.INSTANCE.startPortalScheduler();

			UserTagManager.runTask();

			// [메인 서버 전용 리스너 등록]
			registerListeners();

			// [메인 서버 전용 명령어 등록]
			registerCommands();
		}
	}

	public NamespacedKey getNamespacedKey(String key) {
		return new NamespacedKey(this, key);
	}

	public void playConsoleSound() {
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	public void infoMsg(InfoLevel level, @NotNull Player p, String msg) {
		p.sendMessage(ColorUtils.chat(level.Prefix() + " " + msg));
		p.playSound(p, level.Sound(), 1.0f, 1.0f);
	}

	public @NotNull String getServerType() {
		saveDefaultConfig();
		return getConfig().getString("server-type", "main");
	}

	@Reflection(comment = "Listeners Registration")
	private void registerListeners() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());

			for (var classInfo : classPath.getTopLevelClassesRecursive("xmasLegacy")) {
				Class<?> clazz = classInfo.load();

				if (!Listener.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Listeners.class)) continue;
				if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

				var listenerInstance = clazz.getDeclaredConstructor().newInstance();
				Bukkit.getPluginManager().registerEvents((Listener) listenerInstance, this);

				this.getSLF4JLogger().info("Listener {} Automatically registered", clazz.getSimpleName());
			}
		} catch (Exception e) {
			this.getSLF4JLogger().error("Error occurred while registering all listeners", e);
		}
	}

	@Reflection(comment = "Commands Registration")
	private void registerCommands() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());

			for (var classInfo : classPath.getTopLevelClassesRecursive("xmasLegacy")) {
				Class<?> clazz = classInfo.load();

				if (!CommandExecutor.class.isAssignableFrom(clazz)) continue;
				if (!clazz.isAnnotationPresent(Commands.class)) continue;
				if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

				Commands autoCommand = clazz.getAnnotation(Commands.class);
				String cmdName = autoCommand.command();

				var pluginCommand = this.getCommand(cmdName);
				if (pluginCommand == null) continue;

				var commandInstance = clazz.getDeclaredConstructor().newInstance();
				pluginCommand.setExecutor((CommandExecutor) commandInstance);

				if (TabCompleter.class.isAssignableFrom(clazz)) {
					pluginCommand.setTabCompleter((TabCompleter) commandInstance);
					this.getSLF4JLogger().info("Command {} Automatically registered", cmdName);
				}
			}
		} catch (Exception e) {
			this.getSLF4JLogger().error("Error occurred while registering all Commands/TabCompleter", e);
		}
	}
}