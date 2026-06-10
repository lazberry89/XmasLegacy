package xmasLegacy;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserRepository;
import xmasLegacy.Cosmetics.CosmeticManager;
import xmasLegacy.Cosmetics.CosmeticsCommand;
import xmasLegacy.Cosmetics.TestHeadCommand;
import xmasLegacy.Economy.Currency.OperatorCurrency;
import xmasLegacy.Enchant.EnchantCommand;
import xmasLegacy.Enchant.EnchantListener;
import xmasLegacy.Enchant.EnchantManager;
import xmasLegacy.Env.ConsumableManager;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.FirstRoleManager.FirstRoleManager;
import xmasLegacy.FirstRoleManager.Merchant.*;
import xmasLegacy.FirstRoleManager.Priest.*;
import xmasLegacy.FirstRoleManager.Priest.ShopListener;
import xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;
import xmasLegacy.Gacha.GachaCommand;
import xmasLegacy.Gacha.GachaListener;
import xmasLegacy.Gacha.GachaManager;
import xmasLegacy.HuntingZone.CustomMobs.MobRepository;
import xmasLegacy.HuntingZone.HuntingZoneManager;
import xmasLegacy.HuntingZone.MobSpawnManager;
import xmasLegacy.HuntingZone.ZoneCommandManager;
import xmasLegacy.Lobby.LobbyCommand;
import xmasLegacy.Lobby.LobbyListener;
import xmasLegacy.Lobby.LobbyManager;
import xmasLegacy.PlayerUtils.BagCommandManager;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.Region.*;
import xmasLegacy.RoleSelection.RoleCommand;
import xmasLegacy.RoleSelection.RoleSelectCommand;
import xmasLegacy.RoleSelection.RoleViewDesign;
import xmasLegacy.RoleSelection.SelectListener;
import xmasLegacy.RoleSwitch.BookCommand;
import xmasLegacy.RoleSwitch.DeleteStandCommand;
import xmasLegacy.RoleSwitch.ExpManager;
import xmasLegacy.RoleSwitch.MagicBook;
import xmasLegacy.SecondaryRoleManager.SecondRoleManager;
import xmasLegacy.SecondaryRoleManager.SkillListeners.SecondTestCommand;
import xmasLegacy.SecondaryRoleManager.SkillListeners.SecondaryRoleListener;
import xmasLegacy.ServerPrefix.ChatPrefixListener;
import xmasLegacy.ServerPrefix.PrefixCommand;
import xmasLegacy.ServerPrefix.PrefixManager;
import xmasLegacy.ServerPrefix.UserTagManager;

@SuppressWarnings({"FieldCanBeLocal, DataFlowIssue"})
public final class XmasLegacy extends JavaPlugin {
	private static XmasLegacy instance;

	public static XmasLegacy getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungeecord:main");
		instance = this;

		UserManager.getInstance();
		RuleManager.getInstance();
		InquiryManager.getInstance();
		PrefixManager.getInstance();

		if (AgeableCrops.RegisterRecipe()) {
			getSLF4JLogger().info("Recipe Registered!");
		} else {
			getSLF4JLogger().error("Recipe Not Registered!");
		}

		// 2. 공통 이벤트 리스너 등록
		getServer().getPluginManager().registerEvents(new ServerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new ChatPrefixListener(), this);

		// 3. 공통 명령어 등록
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
		var rm = RegionManager.getInstance();
		var cm = ConsumableManager.getInstance();
		var bg = BagManager.getInstance();
		var msm = MobSpawnManager.getInstance();
		if (rm != null) {
			rm.saveAll();
		}
		if (UserManager.getInstance() != null) {
			UserRepository repository = new SqlUserRepository();
			UserManager.getInstance().getAllUsers().forEach(repository::saveUser);
			getSLF4JLogger().info("모든 유저 데이터를 자동 저장했습니다.");
		}
		if (cm != null) {
			cm.stopCookieTimer();
		}
		if (bg != null) {
			bg.saveAllBags();
			getSLF4JLogger().info("모든 가방 데이터를 자동 저장했습니다.");
		}
		if (msm != null) {
			getSLF4JLogger().info("사냥터 몹 스폰을 종료합니다.");
			msm.stopTask();
		}
		UserTagManager.stopTask();
	}

	private void serverType() {
		saveDefaultConfig();
		String serverType = getConfig().getString("server-type", ServerType.MAIN.str());

		// ------------------ [LOBBY MODE] ------------------
		if (serverType.equals(ServerType.LOBBY.str())) {
			getLogger().warning("Lobby 모드로 시작합니다.");
			LobbyManager.getInstance();

			// 로비 전용 리스너 등록
			getServer().getPluginManager().registerEvents(new LobbyListener(), this);

			// 로비 전용 명령어 등록
			getCommand("lobby").setExecutor(new LobbyCommand());
			getCommand("lobby").setTabCompleter(new LobbyCommand());

			getSLF4JLogger().warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);

			// ------------------ [MAIN GAME MODE] ------------------
		} else if (serverType.equals(ServerType.MAIN.str())) {
			getLogger().warning("Main 모드로 시작합니다.");
			getSLF4JLogger().warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);

			// [메인 서버 전용 인스턴스 초기화]
			RoleViewDesign.getInstance().init();

			BagManager.getInstance();
			SkillEffectManager.getInstance();
			RegionManager.getInstance().startGlobalIndicatorTask();
			ConsumableManager.getInstance();
			GhostModeManager.getInstance();

			EconomyManager.getInstance();
			PartyManager.getInstance();
			ConductableItems.getInstance();
			PriestShopManager.getInstance();
			PriceInterface.getInstance();
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

			ConsumableManager.getInstance().runCookieTimer(this);
			BagManager.getInstance().loadAllBags();

			// 사냥터 몹 초기화
			MobRepository.getInstance().init();

			HuntingZoneManager.getInstance().init();
			MobSpawnManager.getInstance().startTask();

			// [메인 서버 전용 리스너 등록]
			var pm = getServer().getPluginManager();
			pm.registerEvents(new FirstRoleListener(), this);
			pm.registerEvents(ConsumableManager.getInstance(), this);
			pm.registerEvents(new RegionPermissionListener(), this);
			pm.registerEvents(new RegionIndicator(), this);
			pm.registerEvents(new GhostListener(), this);
			pm.registerEvents(new PotionListener(), this);
			pm.registerEvents(new ShopListener(), this);
			pm.registerEvents(new xmasLegacy.FirstRoleManager.Merchant.ShopListener(), this);
			pm.registerEvents(new StockListener(), this);
			pm.registerEvents(new SelectListener(), this);
			pm.registerEvents(new GachaListener(), this);
			pm.registerEvents(new SecondaryRoleListener(), this);
			pm.registerEvents(new EffectListener(), this);
			pm.registerEvents(new EnchantListener(), this);
			pm.registerEvents(new RegionCreateListener(), this);
			pm.registerEvents(new RegionSettingListener(), this);
			pm.registerEvents(new RegionDeleteConfirmListener(), this);

			// [메인 서버 전용 명령어 등록]
			var bag = new BagCommandManager();
			getCommand("가방").setExecutor(bag);
			getCommand("가방").setTabCompleter(bag);
			getCommand("test").setExecutor(new TestCommands());
			var region = new RegionCommandManager();
			getCommand("구역").setExecutor(region);
			getCommand("구역").setTabCompleter(region);
			getCommand("vanish").setExecutor(new GhostCommand());
			var priest = new PriestCommand();
			getCommand("potion").setExecutor(priest);
			getCommand("potion").setTabCompleter(priest);
			getCommand("system").setExecutor(new PriestSystemShopCommand());
			getCommand("shop").setExecutor(new TempCommand());
			var oc = new OperatorCurrency();
			getCommand("currency").setExecutor(oc);
			getCommand("currency").setTabCompleter(oc);
			var cos = new CosmeticsCommand();
			getCommand("cos").setExecutor(cos);
			getCommand("cos").setTabCompleter(cos);
			getCommand("상점").setExecutor(new ShopCommand());
			getCommand("직업선택").setExecutor(new RoleSelectCommand());
			var role = new RoleCommand();
			getCommand("role").setExecutor(role);
			getCommand("role").setTabCompleter(role);
			var gacha = new GachaCommand();
			getCommand("gacha").setExecutor(gacha);
			getCommand("gacha").setTabCompleter(gacha);
			getCommand("head").setExecutor(new TestHeadCommand());
			getCommand("book").setExecutor(new BookCommand());
			getCommand("delstand").setExecutor(new DeleteStandCommand());
			var prefix = new PrefixCommand();
			getCommand("prefix").setExecutor(prefix);
			getCommand("prefix").setTabCompleter(prefix);
			getCommand("0947345").setExecutor(new UserLoadCommand());
			getCommand("second").setExecutor(new SecondTestCommand());
			getCommand("강화").setExecutor(new EnchantCommand());
            var zcm = new ZoneCommandManager();
            getCommand("zone").setExecutor(zcm);
            getCommand("zone").setTabCompleter(zcm);
			UserTagManager.runTask();
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
}