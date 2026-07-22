package org.lazberry.xmaslegacy.Utils;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.LrfInitializer;
import org.lazberry.xmaslegacy.ServerPrefix.PrefixManager;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserSaveManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.FrameworkExceptions.NotValidInitializeTimingException;

@Registry
public class InitiateUtils implements LrfInitializer {
	private final @NotNull PartyManager pm;
	private final @NotNull UserManager um;
	private final @NotNull UserSaveManager us;
	private final @NotNull PrefixManager pfm;

	@Inject
	public InitiateUtils(@NotNull PartyManager pm, @NotNull UserManager um, @NotNull UserSaveManager us, @NotNull PrefixManager pfm) {
		this.pm = pm;
		this.um = um;
		this.us = us;
		this.pfm = pfm;
	}

	@Override
	public void afterPropertiesSet() throws NotValidInitializeTimingException {
		ServerTransfer.setPm(pm);
		ServerTransfer.setUm(um);
		UserHandler.setUs(us);
		UserHandler.setPfm(pfm);
	}
}
