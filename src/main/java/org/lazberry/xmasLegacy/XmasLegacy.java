package org.lazberry.xmasLegacy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmasLegacy.FirstRoleManager.*;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.FirstRoleListener;
import org.lazberry.xmasLegacy.FirstRoleManager.SkillListeners.TestCommands;

public final class XmasLegacy extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");

		ServerJoinManager SJM = new ServerJoinManager();
		RuleManager RM = new RuleManager(this);
		InquiryManager IM = new InquiryManager(RM, this);
		InquiryCommandManager ICM = new InquiryCommandManager(IM);
		InquireTeleportCommand ITC = new InquireTeleportCommand(IM);
        RuleCommandManager RCM = new RuleCommandManager(RM);
        LogCommandManager LCM = new LogCommandManager(IM, this);
        UserManager UM = new UserManager(this);
        SJM.setUM(UM);
        SkillEffectManager SEM =  new SkillEffectManager(this);

		Archer archer = new Archer(4, 4, this);
		Knight knight = new Knight(SEM, this);
		Rogue rogue = new Rogue(4, 4, SEM, this);
		Warrior warrior = new Warrior(4, 4, this);
        Mage mage = new Mage(4, 4,this, SEM);

		getServer().getPluginManager().registerEvents(SJM, this);
		getCommand("문의").setExecutor(ICM);
		getCommand("이동문의").setExecutor(ITC);
        getCommand("filter").setExecutor(RCM);
        getCommand("filter").setTabCompleter(RCM);
        getCommand("log").setExecutor(LCM);
        getCommand("log").setTabCompleter(LCM);

		/// /tests
		TestCommands TC = new TestCommands(SEM, this);
		FirstRoleListener FRL = new FirstRoleListener(SEM, this, knight, rogue, archer, warrior);
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
		// Plugin shutdown logic
	}
}
