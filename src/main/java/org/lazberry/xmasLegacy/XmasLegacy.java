package org.lazberry.xmasLegacy;

import org.bukkit.plugin.java.JavaPlugin;

public final class XmasLegacy extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info("§7[§aX§cm§aa§cs§aL§ce§ag§ca§ac§cy§7]§f Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");

		ServerJoinManager SJM = new ServerJoinManager();
		RuleManager RM = new RuleManager();
		InquiryManager IM = new InquiryManager(RM);
		InquiryCommandManager ICM = new InquiryCommandManager(IM);
		InquireTeleportCommand ITPC = new InquireTeleportCommand(IM);

		getServer().getPluginManager().registerEvents(SJM, this);
		getCommand("문의").setExecutor(ICM);
		getCommand("이동문의").setExecutor(ITPC);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
