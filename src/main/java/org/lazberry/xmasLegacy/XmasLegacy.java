package org.lazberry.xmasLegacy;

import org.bukkit.plugin.java.JavaPlugin;

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

		getServer().getPluginManager().registerEvents(SJM, this);
		getCommand("문의").setExecutor(ICM);
		getCommand("이동문의").setExecutor(ITC);
        getCommand("filter").setExecutor(RCM);
        getCommand("filter").setTabCompleter(RCM);
        getCommand("log").setExecutor(LCM);
        getCommand("log").setTabCompleter(LCM);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
