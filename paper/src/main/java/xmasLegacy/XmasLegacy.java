package xmasLegacy;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.*;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserRepository;
import xmasLegacy.Cosmetics.CosmeticManager;
import xmasLegacy.Cosmetics.CosmeticsCommand;
import xmasLegacy.Cosmetics.TestHeadCommand;
import xmasLegacy.Economy.Currency.OperatorCurrency;
import xmasLegacy.Env.ConsumableManager;
import xmasLegacy.FirstRoleManager.*;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Merchant.*;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.*;
import xmasLegacy.FirstRoleManager.Priest.ShopListener;
import xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;
import xmasLegacy.Gacha.GachaCommand;
import xmasLegacy.Gacha.GachaListener;
import xmasLegacy.Gacha.GachaManager;
import xmasLegacy.Lobby.LobbyCommand;
import xmasLegacy.Lobby.LobbyListener;
import xmasLegacy.Lobby.LobbyManager;
import xmasLegacy.PlayerUtils.BagCommandManager;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.Region.*;
import xmasLegacy.RoleSelection.RoleCommand;
import xmasLegacy.RoleSelection.RoleSelectCommand;
import xmasLegacy.RoleSelection.SelectListener;
import xmasLegacy.RoleSwitch.BookCommand;
import xmasLegacy.RoleSwitch.DeleteStandCommand;
import xmasLegacy.RoleSwitch.MagicBook;
import xmasLegacy.RoleSwitch.ExpManager;
import xmasLegacy.SecondaryRoleManager.Berserker;
import xmasLegacy.SecondaryRoleManager.Defender;
import xmasLegacy.SecondaryRoleManager.Guardian;
import xmasLegacy.SecondaryRoleManager.SecondRoleManager;
import xmasLegacy.SecondaryRoleManager.SkillListeners.SecondTestCommand;
import xmasLegacy.SecondaryRoleManager.SkillListeners.SecondaryRoleListener;
import xmasLegacy.ServerPrefix.ChatPrefixListener;
import xmasLegacy.ServerPrefix.PrefixCommand;
import xmasLegacy.ServerPrefix.PrefixManager;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
public final class XmasLegacy extends JavaPlugin {

	public Archer archer;
	public Knight knight;
	public Rogue rogue;
	public Warrior warrior;
	public Mage mage;
	public Priest priest;
	public Farmer farmer;
	public Miner miner;
	public Gatherer gatherer;
	public Merchant merchant;
	public Crafter crafter;

	public Defender defender;
	public Guardian guardian;
	public Berserker berserker;

	private static XmasLegacy instance;

	public static XmasLegacy getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		// 1. 공통 필수 초기화 (어떤 서버 타입이든 기본으로 쓰는 고정 매니저들)
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

		// 4. 서버 타입 정밀 분석 후 격리 기동 시작
		serverType();

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
	}

	@Override
	public void onDisable() {
		var rm = RegionManager.getInstance();
		var cm = ConsumableManager.getInstance();
		var bg = BagManager.getInstance();
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
	}

	private void serverType() {
		saveDefaultConfig();
		String serverType = getConfig().getString("server-type", "main");

		// ------------------ [LOBBY MODE] ------------------
		if (serverType.equals("lobby")) {
			getLogger().warning("Lobby 모드로 시작합니다.");
			LobbyManager.getInstance();

			// 로비 전용 리스너 등록
			getServer().getPluginManager().registerEvents(new LobbyListener(), this);

			// 로비 전용 명령어 등록
			getCommand("lobby").setExecutor(new LobbyCommand());
			getCommand("lobby").setTabCompleter(new LobbyCommand());

			getSLF4JLogger().warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);

			// ------------------ [MAIN GAME MODE] ------------------
		} else if (serverType.equals("main")) {
			getLogger().warning("Main 모드로 시작합니다.");
			getSLF4JLogger().warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);

			// [메인 서버 전용 인스턴스 초기화]
			BagManager.getInstance();
			SkillEffectManager.getInstance();
			RegionManager.getInstance();
			ConsumableManager.getInstance();
			RegionPreviewer.getInstance();
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
			this.archer = new Archer();
			this.knight = new Knight();
			this.rogue  = new Rogue();
			this.mage = new Mage();
			this.warrior = new Warrior();
			this.priest = new Priest();
			this.farmer = new Farmer();
			this.miner = new Miner();
			this.gatherer = new Gatherer();
			this.merchant = new Merchant();
			this.crafter = new Crafter();
			FirstRoleManager.getInstance().init();

			// SecondaryRole 초기화
			this.defender = new Defender();
			this.guardian = new Guardian();
			this.berserker = new Berserker();
			SecondRoleManager.getInstance().init();


			// Gacha 초기화
			GachaManager.getInstance();

			//this.TC.setPM(PM); TestCommands
			//this.LCM.setRM(RGM); LogCommandManager
			ConsumableManager.getInstance().runCookieTimer(this);
			BagManager.getInstance().loadAllBags();

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

	public String getServerType() {
		saveDefaultConfig();
		return getConfig().getString("server-type", "lobby");
	}
}