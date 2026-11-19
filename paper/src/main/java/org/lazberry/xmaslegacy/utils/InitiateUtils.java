package org.lazberry.xmaslegacy.utils;

import org.lazberry.xmaslegacy.framework.LrfInitializer;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.chat.prefix.PrefixManager;
import org.lazberry.xmaslegacy.bags.BagMailbox;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.exception.NotValidInitializeTimingException;
import org.lazberry.xmaslegacy.framework.ServerType;

@Components.Exclude(type = ServerType.LOBBY)
public class InitiateUtils implements LrfInitializer {
	private final PartyManager pm;
	private final UserManager um;
	private final UserSaveManager us;
	private final PrefixManager pfm;
	private final BagManager bm;
	private final BagMailbox mailbox;

	@Inject
    public InitiateUtils(PartyManager pm, UserManager um, UserSaveManager us, PrefixManager pfm, BagManager bm, BagMailbox mailbox) {
        this.pm = pm;
        this.um = um;
        this.us = us;
        this.pfm = pfm;
        this.bm = bm;
        this.mailbox = mailbox;
    }

    @Override
	public void afterPropertiesSet() throws NotValidInitializeTimingException {
		ServerTransfer.setPm(pm);
		ServerTransfer.setUm(um);
		UserHandler.setUs(us);
		UserHandler.setPfm(pfm);
		InventoryHelper.setBm(bm);
		InventoryHelper.setMailbox(mailbox);
	}
}
