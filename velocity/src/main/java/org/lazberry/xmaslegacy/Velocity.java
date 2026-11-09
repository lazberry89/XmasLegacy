package org.lazberry.xmaslegacy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.LazberryRegistryFramework;
import org.lazberry.xmaslegacy.party.TpaActualTeleportManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
		id = "xmaslegacy-velocity",
		name = "XmasLegacy-Velocity",
		version = "1.0-SNAPSHOT",
		authors = {"Lazberry"}
)
public class Velocity {
	private static Velocity instance;
	private static ProxyServer proxy;
	private static Logger log;
	private static Path dataDirectory;
	private static Path configPath;

	public static Velocity getVelocityInstance() {
		return instance;
	}

	public static ProxyServer getInstance() {
		return proxy;
	}

	public static Logger logger() {
		return log;
	}

	public static Path getConfig() {
		return configPath;
	}

	public static Path directory() {
		return dataDirectory;
	}

	@Inject
	public Velocity(ProxyServer proxyServer, Logger logger, Path path) {
		initConfig();

		instance = this;
		log = logger;
		proxy = proxyServer;
		dataDirectory = path;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		LazberryRegistryFramework.boot(getInstance(), getInstance().getClass());
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		LazberryRegistryFramework.cleanUp(getInstance(), getInstance().getClass());
	}

	private void initConfig() {
		try {

			if (!Files.exists(dataDirectory)) {
				Files.createDirectories(dataDirectory);
			}

			configPath = dataDirectory.resolve("config.yml");
			if (!Files.exists(configPath)) {
				try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
					if (in != null) {
						Files.copy(in, configPath);
						log.info("General setting config file created: {}", configPath.toAbsolutePath());
					} else {
						Files.createFile(configPath);
					}
				}
			}
		} catch (IOException e) {
			log.error("설정 파일 초기화 중 오류가 발생했습니다.", e);
		}
	}
}













