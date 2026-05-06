package xmasLegacy;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.*;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Inquiry.InquiryRepository;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.Env.ConsumableManager;
import xmasLegacy.FirstRoleManager.*;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Merchant.PriceInterface;
import xmasLegacy.FirstRoleManager.Merchant.TempCommand;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.*;
import xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;
import xmasLegacy.Lobby.LobbyCommand;
import xmasLegacy.Lobby.LobbyListener;
import xmasLegacy.Lobby.LobbyManager;
import xmasLegacy.PlayerUtils.BagCommandManager;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.Region.*;

import java.util.ArrayList;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
public final class XmasLegacy extends JavaPlugin {

    private ServerJoinManager SJM;
    private RuleManager RM;
    private InquiryManager IM;
    private InquiryCommandManager ICM;
    private InquireTeleportCommand ITC;
	private InquiryRepository IR;
    private RuleCommandManager RCM;
    private LogCommandManager LCM;
    private UserManager UM;
	private BagManager BM;
	private BagCommandManager BCM;
    private SkillEffectManager SEM;
    private RegionManager RGM;
	private FirstRoleListener FRL;
	private TestCommands TC;
	private ConsumableManager CM;
	private RegionPermission RP;
	private RegionCommandManager RGCM;
	private RegionIndicator RI;
    private RegionPreviewer RGP;
    private GhostModeManager GMM;
    private GhostCommand GC;
    private GhostListener GL;
    private EconomyManager EM;
    private PartyManager PM;
    private ConductableItems CDI;
	private PriestShopManager PSM;
	private PotionListener PL;
	private PriestCommand PC;
	private PriestSystemShopCommand PSSC;
	private ShopListener SL;
	private PriceInterface PCI;
	private xmasLegacy.FirstRoleManager.Merchant.ShopListener SPL;
	private TempCommand TPC;

    private Archer archer;
    private Knight knight;
    private Rogue rogue;
    private Warrior warrior;
    private Mage mage;
	private Priest priest;
	private Farmer farmer;
	private Miner miner;
	private Gatherer gatherer;

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
        this.RGP = new RegionPreviewer(this,RGM);
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

        this.archer = new Archer(4, 4, this);
        this.knight = new Knight(SEM, this);
        this.rogue  = new Rogue(4, 4, SEM, this);
        this.mage = new Mage(4, 4,this, SEM);
        this.warrior = new Warrior(4, 4, this);
		this.priest = new Priest(4, 4, PM, SEM, this);
		this.farmer = new Farmer(4, 4, this, RGM);
		this.miner = new Miner(4, 4, this);
		this.gatherer = new Gatherer(4, 4, this);

		if (AgeableCrops.RegisterRecipe()) {
			getSLF4JLogger().info("Recipe Registered!");
		} else {
			getSLF4JLogger().error("Recipe Not Registered!");
		}
		this.FRL = new FirstRoleListener(this, knight, rogue, archer, warrior, mage, priest, farmer, miner, gatherer);
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

	public String getServerType() {
		saveDefaultConfig();
		return getConfig().getString("server-type", "lobby");
	}
}
