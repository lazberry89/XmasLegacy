package org.lazberry.xmasLegacy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmasLegacy.Env.ConsumableManager;
import org.lazberry.xmasLegacy.FirstRoleManager.*;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;
import org.lazberry.xmasLegacy.PlayerUtils.BagCommandManager;
import org.lazberry.xmasLegacy.PlayerUtils.BagManager;
import org.lazberry.xmasLegacy.Region.*;

public final class XmasLegacy extends JavaPlugin {

    private ServerJoinManager SJM;
    private RuleManager RM;
    private InquiryManager IM;
    private InquiryCommandManager ICM;
    private InquireTeleportCommand ITC;
    private RuleCommandManager RCM;
    private LogCommandManager LCM;
    private UserManager UM;
	private BagManager BM;
	private BagCommandManager BCM;
    private SkillEffectManager SEM;
    private RegionManager RGM;
	private FirstRoleListener FRL;
	private TestCommands TC;
	private ChatCensoring CC;
	private ConsumableManager CM;
	private RegionPermission RP;
	private RegionCommandManager RGCM;
	private RegionIndicator RI;

    private Archer archer;
    private Knight knight;
    private Rogue rogue;
    private Warrior warrior;
    private Mage mage;

	@Override
	public void onEnable() {
        this.SJM = new ServerJoinManager();
        this.RM = new RuleManager(this);
        this.IM = new InquiryManager(RM, this);
        this.ICM = new InquiryCommandManager(IM);
        this.ITC = new InquireTeleportCommand(IM);
        this.RCM = new RuleCommandManager(RM);
        this.LCM = new LogCommandManager(IM, this);
        this.UM = new UserManager(this);
		this.BM = new BagManager(this);
		this.BCM = new BagCommandManager(BM);
        this.SEM = new SkillEffectManager(this);
        this.RGM = new RegionManager(this, UM);
		this.CC = new ChatCensoring(RM, this);
		this.CM = new ConsumableManager(this, UM, BM);
		this.RP = new RegionPermission(RGM);
		this.RGCM = new RegionCommandManager(RGM);
		this.RI = new RegionIndicator(RGM, UM, this);

        this.archer = new Archer(4, 4, this);
        this.knight = new Knight(SEM, this);
        this.rogue  = new Rogue(4, 4, SEM, this);
        this.mage = new Mage(4, 4,this, SEM);
        this.warrior = new Warrior(4, 4, this);

		this.FRL = new FirstRoleListener(this, knight, rogue, archer, warrior, mage);
		this.TC = new TestCommands(SEM, this);

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
        SJM.setUM(UM);
        UM.getAllUsers();
		CM.runCookieTimer(this);
		this.BM.loadAllBags();
		this.LCM.setRM(RGM);

		getServer().getPluginManager().registerEvents(SJM, this);
		getServer().getPluginManager().registerEvents(FRL, this);
		getServer().getPluginManager().registerEvents(CC, this);
		getServer().getPluginManager().registerEvents(CM, this);
		getServer().getPluginManager().registerEvents(RP, this);
		getServer().getPluginManager().registerEvents(RI, this);

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

        new BukkitRunnable() {
            @Override
            public void run() {
                UM.getAllUsers().forEach(UM::saveUserToFile);
                getLogger().info("모든 유저 데이터를 자동 저장했습니다.");
            }
        }.runTaskTimer(this, 20L * 60 * 5, 20L * 60 * 5);
	}

	@Override
	public void onDisable() {
        if (RGM != null) {
            RGM.saveAll();
        }
		UM.getAllUsers().forEach(UM::saveUserToFile);
		getLogger().info("모든 유저 데이터를 자동 저장했습니다.");
		CM.stopCookieTimer();
		BM.saveAllBags();
		getLogger().info("모든 가방 데이터를 자동 저장했습니다.");
	}
}
