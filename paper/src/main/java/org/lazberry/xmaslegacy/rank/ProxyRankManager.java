package org.lazberry.xmaslegacy.rank;

import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Registry.Exclude(type = ServerType.LOBBY)
public class ProxyRankManager implements Initiator {

	@Override
	public void init() {

	}

	@Override
	public void close() {
		Initiator.super.close();
	}
}
