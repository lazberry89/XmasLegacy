package org.lazberry.xmaslegacy.utils;

import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.LrfInitializer;
import org.lazberry.xmaslegacy.ServerPrefix.PrefixManager;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.FrameworkExceptions.NotValidInitializeTimingException;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Exclude(type = ServerType.LOBBY)
public class InitiateUtils implements LrfInitializer {
	private final PartyManager pm;
	private final UserManager um;
	private final UserSaveManager us;
	private final PrefixManager pfm;
	private final BagManager bm;

	@Inject
    public InitiateUtils(PartyManager pm, UserManager um, UserSaveManager us, PrefixManager pfm, BagManager bm) {
        this.pm = pm;
        this.um = um;
        this.us = us;
        this.pfm = pfm;
        this.bm = bm;
    }

    @Override
	public void afterPropertiesSet() throws NotValidInitializeTimingException {
		ServerTransfer.setPm(pm);
		ServerTransfer.setUm(um);
		UserHandler.setUs(us);
		UserHandler.setPfm(pfm);
		Casino.setBm(bm);
		InventoryHelper.setBm(bm);
	}
}
