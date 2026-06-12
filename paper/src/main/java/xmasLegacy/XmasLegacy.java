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
import xmasLegacy.Cosmetics.CosmeticManager;
import xmasLegacy.Enchant.EnchantManager;
import xmasLegacy.Env.ConsumableManager;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.FirstRoleManager.FirstRoleManager;
import xmasLegacy.FirstRoleManager.Merchant.MerchantStockInterface;
import xmasLegacy.FirstRoleManager.Merchant.PriceManager;
import xmasLegacy.FirstRoleManager.Priest.PriestShopManager;
import xmasLegacy.Gacha.GachaManager;
import xmasLegacy.HuntingZone.CustomMobs.MobRepository;
import xmasLegacy.HuntingZone.HuntingZoneManager;
import xmasLegacy.HuntingZone.MobSpawnManager;
import xmasLegacy.Lobby.LobbyCommand;
import xmasLegacy.Lobby.LobbyListener;
import xmasLegacy.Lobby.LobbyManager;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.RoleSelection.RoleViewDesign;
import xmasLegacy.RoleSwitch.ExpManager;
import xmasLegacy.RoleSwitch.MagicBook;
import xmasLegacy.SecondaryRoleManager.SecondRoleManager;
import xmasLegacy.ServerPrefix.ChatPrefixListener;
import xmasLegacy.ServerPrefix.UserTagManager;
import xmasLegacy.TransferPortal.PortalManager;

@SuppressWarnings({"FieldCanBeLocal, DataFlowIssue"})
public final class XmasLegacy extends JavaPlugin {
	private static XmasLegacy instance;
	private RegionManager regionManager;
	private ConsumableManager consumableManager;
	private BagManager bagManager;
	private MobSpawnManager mobSpawnManager;

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
		var rule = new RuleCommandManager();
		getCommand("filter").setExecutor(rule);
		getCommand("filter").setTabCompleter(rule);
		var log = new LogCommandManager();
		getCommand("log").setExecutor(log);
		getCommand("log").setTabCompleter(log);
		serverType();

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
	}

	@Override
	public void onDisable() {
		if (this.regionManager != null) {
			this.regionManager.saveAll();
		}

		UserRepository repository = new SqlUserRepository();
		UserManager.INSTANCE.getAllUsers().forEach(repository::saveUser);
		getSLF4JLogger().info("모든 유저 데이터를 자동 저장했습니다.");

		if (this.consumableManager != null) {
			this.consumableManager.stopCookieTimer();
		}
		if (this.bagManager != null) {
			this.bagManager.saveAllBags();
			getSLF4JLogger().info("모든 가방 데이터를 자동 저장했습니다.");
		}
		if (this.mobSpawnManager != null) {
			getSLF4JLogger().info("사냥터 몹 스폰을 종료합니다.");
			this.mobSpawnManager.stopTask();
		}
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
			var mobRepository = new MobRepository();
			var huntingZoneManager = new HuntingZoneManager();
			RoleViewDesign.getInstance().init();

			BagManager.getInstance();
			SkillEffectManager.getInstance();

			this.regionManager = new RegionManager();
			this.regionManager.startGlobalIndicatorTask();
			this.bagManager = new BagManager();
			this.consumableManager = new ConsumableManager(bagManager);
			this.mobSpawnManager = new MobSpawnManager(huntingZoneManager, mobRepository);

			var priestShopManager = new PriestShopManager();
			var priceManager = new PriceManager();
			CosmeticManager.getInstance();
			MerchantStockInterface.getInstance();
			ExpManager.getInstance();
			MagicBook.getInstance();

			// FirstRole 초기화
			FirstRoleManager.getInstance().init();

			// SecondaryRole 초기화
			SecondRoleManager.getInstance().init();

			// Gacha 초기화
			GachaManager.getInstance();

			EnchantManager.getInstance();

			consumableManager.runCookieTimer(this);
			BagManager.getInstance().loadAllBags();

			// 사냥터 몹 초기화
			mobRepository.init();

			huntingZoneManager.init();
			mobSpawnManager.startTask();
			PortalManager.getInstance().startPortalScheduler();

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

				this.getSLF4JLogger().info("리스너 {} 가 자동 등록되었습니다.", clazz.getSimpleName());
			}
		} catch (Exception e) {
			this.getSLF4JLogger().error("Error occurred while registering all listeners", e);
		}
	}

	@Reflection(comment = "Commands Registration")
	private void registerCommands() {
		try {
			var classPath = com.google.common.reflect.ClassPath.from(this.getClassLoader());

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
					this.getSLF4JLogger().info("커맨드 {} 가 자동 등록되었습니다.", cmdName);
				}
			}
		} catch (Exception e) {
			this.getSLF4JLogger().error("Error occurred while registering all Commands/TabCompleter", e);
		}
	}
}