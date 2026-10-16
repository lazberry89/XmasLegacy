package org.lazberry.xmaslegacy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lazberry.xmaslegacy.party.TpaActualTeleportManager;
import org.slf4j.Logger;

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

	public static Velocity getVelocityInstance() {
		return instance;
	}

	public static ProxyServer getInstance() {
		return proxy;
	}

	public static Logger logger() {
		return log;
	}

	@Inject
	public Velocity(ProxyServer proxyServer, Logger logger) {
		instance = this;
		log = logger;
		proxy = proxyServer;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		JedisService.initiateJedis();

		TpaActualTeleportManager tpaManager = new TpaActualTeleportManager();
		log.info("XmasLegacy Velocity module enabled.");
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		JedisService.closeJedis();
		log.info("XmasLegacy Velocity module disabled and Redis connection closed.");
	}
}













