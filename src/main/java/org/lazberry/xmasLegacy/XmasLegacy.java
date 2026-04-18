package org.lazberry.xmasLegacy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmasLegacy.FirstRoleManager.*;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;
import org.lazberry.xmasLegacy.Region.RegionManager;

public final class XmasLegacy extends JavaPlugin {

    private ServerJoinManager SJM;
    private RuleManager RM;
    private InquiryManager IM;
    private InquiryCommandManager ICM;
    private InquireTeleportCommand ITC;
    private RuleCommandManager RCM;
    private LogCommandManager LCM;
    private UserManager UM;
    private SkillEffectManager SEM;
    private RegionManager RGM;

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
        this.SEM = new SkillEffectManager(this);
        this.RGM = new RegionManager(this, UM);

        this.archer = new Archer(4, 4, this);
        this.knight = new Knight(SEM, this);
        this.rogue  = new Rogue(4, 4, SEM, this);
        this.mage = new Mage(4, 4,this, SEM);
        this.warrior = new Warrior(4, 4, this);

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
        SJM.setUM(UM);
        UM.getAllUsers();

		getServer().getPluginManager().registerEvents(SJM, this);
		getCommand("문의").setExecutor(ICM);
		getCommand("이동문의").setExecutor(ITC);
        getCommand("filter").setExecutor(RCM);
        getCommand("filter").setTabCompleter(RCM);
        getCommand("log").setExecutor(LCM);
        getCommand("log").setTabCompleter(LCM);

		//tests
		TestCommands TC = new TestCommands(SEM, this);
		FirstRoleListener FRL = new FirstRoleListener(SEM, this, knight, rogue, archer, warrior, mage);
		getCommand("test").setExecutor(TC);
		getServer().getPluginManager().registerEvents(FRL, this);




        new BukkitRunnable() {
            @Override
            public void run() {
                UM.getAllUsers().forEach(UM::saveUserToFile);
                getLogger().info("모든 유저 데이터를 자동 저장했습니다.");
            }
        }.runTaskTimer(this, 20L * 60 * 5, 20L * 60 * 5); // 5분
	}

	@Override
	public void onDisable() {
        if (RGM != null) {
            RGM.saveAll();
        }
		// Plugin shutdown logic
	}
}
