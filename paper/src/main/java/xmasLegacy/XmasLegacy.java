package xmasLegacy;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.*;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Inquiry.InquiryRepository;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.UserManager;
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
import xmasLegacy.Gacha.GachaBundleListener;
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

import java.util.ArrayList;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
public final class XmasLegacy extends JavaPlugin {

    public ServerJoinManager SJM;
	public RuleManager RM;
	public InquiryManager IM;
	public InquiryCommandManager ICM;
    public InquireTeleportCommand ITC;
	public InquiryRepository IR;
    public RuleCommandManager RCM;
    public LogCommandManager LCM;
    public UserManager UM;
	public BagManager BM;
	public BagCommandManager BCM;
    public SkillEffectManager SEM;
    public RegionManager RGM;
	public FirstRoleListener FRL;
	public TestCommands TC;
	public ConsumableManager CM;
	public RegionPermission RP;
	public RegionCommandManager RGCM;
	public RegionIndicator RI;
	public RegionPreviewer RGP;
	public GhostModeManager GMM;
	public GhostCommand GC;
	public GhostListener GL;
	public EconomyManager EM;
	public PartyManager PM;
	public ConductableItems CDI;
	public PriestShopManager PSM;
	public PotionListener PL;
	public PriestCommand PC;
	public PriestSystemShopCommand PSSC;
	public ShopListener SL;
	public PriceInterface PCI;
	public xmasLegacy.FirstRoleManager.Merchant.ShopListener SPL;
	public TempCommand TPC;
	public OperatorCurrency OC;
	public CosmeticManager CSM;
	public CosmeticsCommand CCC;
	public UserSellingManager USM;
	public MerchantStockInterface MSI;
	public StockListener SKL;
	public ShopCommand SC;

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

	public RoleManager RLM;
	public SelectListener STL;
	public RoleSelectCommand RSC;
	public RoleCommand RLC;

	//Gacha
	public GachaManager GM;
	public GachaListener GCL;
	public GachaBundleListener GBL;
	public GachaCommand GCC;
	public ExpManager REM;
	public MagicBook MB;
	public BookCommand BC;
	public DeleteStandCommand DSC;

	@Override
	public void onEnable() {
		serverType();

        this.UM = new UserManager();
        this.SJM = new ServerJoinManager(UM, this);
        this.RM = new RuleManager(new ArrayList<>());
		this.IR = new InquiryRepository();
        this.ICM = new InquiryCommandManager(IM);
        this.IM = new InquiryManager(UM, RM, IR);
        this.ITC = new InquireTeleportCommand(IM);
        this.RCM = new RuleCommandManager(RM);
        this.LCM = new LogCommandManager(IM, this);
		this.BM = new BagManager(this);
		this.BCM = new BagCommandManager(BM);
        this.SEM = new SkillEffectManager(this);
        this.RGM = new RegionManager(this, UM);
		this.CM = new ConsumableManager(this, UM, BM);
		this.RP = new RegionPermission(RGM);
		this.RGCM = new RegionCommandManager(RGM);
		this.RI = new RegionIndicator(RGM, UM, this);
		this.TC = new TestCommands(SEM, RGM, this);
        this.RGP = new RegionPreviewer(this, RGM);
        this.GMM = new GhostModeManager(this);
        this.GC = new GhostCommand(GMM);
        this.GL = new GhostListener(GMM, this);
        this.EM = new EconomyManager(UM);
        this.PM = new PartyManager(UM);
        this.CDI = new ConductableItems(this);
		this.PL = new PotionListener(this, CDI);
		this.PC = new PriestCommand(CDI);
		this.PSM = new PriestShopManager(CDI, EM);
		this.PSSC = new PriestSystemShopCommand(PSM);
		this.SL = new ShopListener(PSM, UM, EM, CDI, BM, this);
		this.PCI = new PriceInterface();
		this.SPL = new xmasLegacy.FirstRoleManager.Merchant.ShopListener(PCI, UM, EM);
		this.TPC = new TempCommand(PCI);
		this.OC = new OperatorCurrency(EM);
		this.CSM = new CosmeticManager();
		this.CCC = new CosmeticsCommand(CSM);
		this.USM = new UserSellingManager();
		this.MSI = new MerchantStockInterface(this);
		this.SKL = new StockListener(this);
		this.SC = new ShopCommand(this);

        this.archer = new Archer(4, 4, this);
        this.knight = new Knight(SEM, this);
        this.rogue  = new Rogue(4, 4, SEM, this);
        this.mage = new Mage(4, 4,this, SEM);
        this.warrior = new Warrior(4, 4, this);
		this.priest = new Priest(4, 4, PM, SEM, this);
		this.farmer = new Farmer(4, 4, this, RGM);
		this.miner = new Miner(4, 4, this);
		this.gatherer = new Gatherer(4, 4, this);
		this.merchant = new Merchant(4, 4, this);
		this.crafter = new Crafter(4, 4, this);

		this.RLM = new RoleManager();
		this.STL = new SelectListener(this);
		this.RSC = new RoleSelectCommand(this);
		this.RLC = new RoleCommand(this);

		//Gacha
		this.GM = new GachaManager(this);
		this.GBL = new GachaBundleListener(this);
		this.GCL = new GachaListener(this);
		this.GCC = new GachaCommand(this);

		this.REM = new ExpManager(this);
		this.MB = new MagicBook(this);
		this.BC = new BookCommand(this);
		this.DSC = new DeleteStandCommand(this);

		if (AgeableCrops.RegisterRecipe()) {
			getSLF4JLogger().info("Recipe Registered!");
		} else {
			getSLF4JLogger().error("Recipe Not Registered!");
		}
		this.FRL = new FirstRoleListener(this);
		this.TC.setPM(PM);

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
        UM.getAllUsers();
		CM.runCookieTimer(this);
		this.BM.loadAllBags();
		this.LCM.setRM(RGM);

		getServer().getPluginManager().registerEvents(SJM, this);
		getServer().getPluginManager().registerEvents(FRL, this);
		getServer().getPluginManager().registerEvents(CM, this);
		getServer().getPluginManager().registerEvents(RP, this);
		getServer().getPluginManager().registerEvents(RI, this);
        getServer().getPluginManager().registerEvents(GL, this);
		getServer().getPluginManager().registerEvents(PL, this);
		getServer().getPluginManager().registerEvents(SL, this);
		getServer().getPluginManager().registerEvents(SPL, this);
		getServer().getPluginManager().registerEvents(SKL, this);
		getServer().getPluginManager().registerEvents(STL, this);
		getServer().getPluginManager().registerEvents(GCL, this);

		getCommand("문의").setExecutor(ICM);
		getCommand("이동문의").setExecutor(ITC);
        getCommand("filter").setExecutor(RCM);
        getCommand("filter").setTabCompleter(RCM);
        getCommand("log").setExecutor(LCM);
        getCommand("log").setTabCompleter(LCM);
		getCommand("가방").setExecutor(BCM);
		getCommand("가방").setTabCompleter(BCM);
		getCommand("test").setExecutor(TC);
		getCommand("구역").setExecutor(RGCM);
		getCommand("구역").setTabCompleter(RGCM);
        getCommand("vanish").setExecutor(GC);
		getCommand("potion").setExecutor(PC);
		getCommand("potion").setTabCompleter(PC);
		getCommand("system").setExecutor(PSSC);
		getCommand("shop").setExecutor(TPC);
		getCommand("currency").setExecutor(OC);
		getCommand("currency").setTabCompleter(OC);
		getCommand("cos").setExecutor(CCC);
		getCommand("cos").setTabCompleter(CCC);
		getCommand("상점").setExecutor(SC);
		getCommand("직업선택").setExecutor(RSC);
		getCommand("role").setExecutor(RLC);
		getCommand("role").setTabCompleter(RLC);
		getCommand("gacha").setExecutor(GCC);
		getCommand("gacha").setTabCompleter(GCC);
		getCommand("head").setExecutor(new TestHeadCommand());
		getCommand("book").setExecutor(BC);
		getCommand("delstand").setExecutor(DSC);
	}

	@Override
	public void onDisable() {
        if (RGM != null) {
            RGM.saveAll();
        }
		//UM.getAllUsers().forEach(UM::saveUserToFile);
		getSLF4JLogger().info("모든 유저 데이터를 자동 저장했습니다.");
		if (CM != null) {
			CM.stopCookieTimer();
		}
		if (BM != null) {
			BM.saveAllBags();
		}
		getSLF4JLogger().info("모든 가방 데이터를 자동 저장했습니다.");
	}

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }

    public void playConsoleSound() {
		java.awt.Toolkit.getDefaultToolkit().beep();
    }
	private void serverType() {
		saveDefaultConfig();

		String serverType = getConfig().getString("server-type", "main");

		if (serverType.equals("lobby")) {
			getLogger().warning("Lobby 모드로 시작합니다.");
			LobbyManager LM = new LobbyManager(this);
			getServer().getPluginManager().registerEvents(
					new LobbyListener(this, LM), this);
			LobbyCommand LBC = new LobbyCommand(LM, this);
			getCommand("lobby").setExecutor(LBC);
			getCommand("lobby").setTabCompleter(LBC);
			getSLF4JLogger().warn("server-type = \"lobby\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);
		} else if (serverType.equals("main")) {
			getLogger().warning("Main 모드로 시작합니다.");
			getSLF4JLogger().warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요. 현재값: \"{}\"", serverType);
		}
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
