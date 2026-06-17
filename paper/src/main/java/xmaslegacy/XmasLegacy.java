package xmaslegacy;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
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
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.PluginUtils.ReflectionManager;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.ServerPrefix.ChatPrefixListener;
import xmaslegacy.ServerPrefix.UserTagManager;
import xmaslegacy.Utils.InfoLevel;

import java.io.IOException;

public final class XmasLegacy extends JavaPlugin {

	@Getter
	private static XmasLegacy instance;

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bungeecord:main");
		instance = this;

		UserManager.INSTANCE.initDataFolder(this.getDataFolder());
		ServerInitializer.initiate(this);

		if (AgeableCrops.RegisterRecipe()) getSLF4JLogger().info("Recipe Registered!");
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

		ConsumableManager.INSTANCE.stopCookieTimer();

		BagManager.INSTANCE.saveAllBags();
		getSLF4JLogger().info("Bag data is automatically saved!");

		getSLF4JLogger().info("Stopping Hunting Zone spawning.");
		MobSpawnManager.INSTANCE.stopTask();

		UserTagManager.stopTask();
	}

	public @NotNull NamespacedKey getNamespacedKey(@NotNull String key) {
		return new NamespacedKey(this, key);
	}

	public void infoMsg(@NotNull InfoLevel level, @NotNull Player p, @NotNull String msg) {
		p.sendMessage(ColorUtils.chat(level.Prefix() + " " + msg));
		p.playSound(p, level.Sound(), 1.0f, 1.0f);
	}

	public void registerReflection() {
		try {
			var classPath = ClassPath.from(this.getClassLoader());
			ReflectionManager.registerListeners(classPath);
			ReflectionManager.registerCommands(classPath);
			ReflectionManager.registerAllRoles(classPath);
		} catch (IOException e) {
			this.getSLF4JLogger().error("Error occurred while registering instances! Disabling plugin.", e);
			this.getServer().broadcast(ColorUtils.chat(Alert.RED + " 서버 메인 플러그인 로드를 실패하였습니다. 플러그인을 비활성화합니다."));
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}
}