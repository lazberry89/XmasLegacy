package xmaslegacy;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.SqlUserRepository;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Env.ConsumableManager;
import xmaslegacy.HuntingZone.MobSpawnManager;
import xmaslegacy.Icing.IcingListener;
import xmaslegacy.Icing.IcingSystem;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.PluginUtils.ReflectionManager;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.ServerPrefix.ChatPrefixListener;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.ServerTransfer;

import java.io.IOException;

public final class XmasLegacy extends JavaPlugin {

	@Getter
	private static XmasLegacy instance;

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungeecord:main");
		instance = this;

		UserManager.INSTANCE.initDataFolder(this.getDataFolder());

		//빙결시스템 시작
		IcingSystem.INSTANCE.startTask(this);
		getServer().getPluginManager().registerEvents(new IcingListener(), this);

		ServerInitializer.initiate(this);

		if (AgeableCrops.RegisterRecipe(this)) getSLF4JLogger().info("Recipe Registered!");
		else getSLF4JLogger().error("Recipe Not Registered!");

		getServer().getPluginManager().registerEvents(new ServerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new ChatPrefixListener(), this);

		registerGlobalCommand();

		getLogger().info("XmasLegacy Plugin Enabled!");
		getLogger().warning("This Christmas will be Perfect!");
	}

	private void registerGlobalCommand() {
		var inquiry = getCommand("문의");
		var move = getCommand("이동문의");
		var filter = getCommand("filter");
		var log = getCommand("log");
		if (inquiry != null) inquiry.setExecutor(new InquiryCommandManager());
		if (move != null) move.setExecutor(new InquireTeleportCommand());
		var rule = new RuleCommand();
		if (filter != null) {
			filter.setExecutor(rule);
			filter.setTabCompleter(rule);
		}
		var logCommand = new LogCommand();
		if (log != null) {
			log.setExecutor(logCommand);
			log.setTabCompleter(logCommand);
		}
	}

	@Override
	public void onDisable() {
		RegionManager.INSTANCE.saveAll();

		UserManager.INSTANCE.getAllUsers().forEach(SqlUserRepository.INSTANCE::saveUser);
		getSLF4JLogger().info("User info is automatically saved!");

		IcingSystem.INSTANCE.stopTask();

		ConsumableManager.INSTANCE.stopCookieTimer();

		BagManager.INSTANCE.saveAllBags();
		getSLF4JLogger().info("Bag data is automatically saved!");

		getSLF4JLogger().info("Stopping Hunting Zone spawning.");
		MobSpawnManager.INSTANCE.stopTask();

		//UserTagManager.stopTask();
	}

	public @NotNull NamespacedKey getNamespacedKey(@NotNull String key) {
		return new NamespacedKey(this, key);
	}

	public void infoMsg(@NotNull InfoLevel level, @NotNull Player p, @NotNull String msg) {
		Component txt = ColorUtils.chat(level.Prefix() + " " + msg);
		p.sendMessage(txt);
		p.playSound(p, level.Sound(), 1.0f, 1.0f);
		var user = UserManager.INSTANCE.getUser(p.getUniqueId());
		if (user == null) {
			ServerTransfer.sendReloadNotice(p);
			return;
		}
		if (user.isMobile()) p.sendActionBar(txt);
	}

	public void registerReflection() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());
			ReflectionManager.registerListeners(classPath);
			ReflectionManager.registerCommands(classPath);
			ReflectionManager.registerAllRoles(classPath);
		} catch (IOException e) {
			this.getSLF4JLogger().error("Error occurred while registering instances! Disabling plugin.", e);
			this.getServer().broadcast(ColorUtils.chat(Alert.RED + " Failed to load main plugin System. Disabling plugin."));
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}
}