package org.lazberry.xmaslegacy.rank.board;

import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.ProxyRankManager;

@Registry.Exclude(type = ServerType.LOBBY)
public class RankBoardManager implements Initiator {
    private final ProxyRankManager rankManager;

    @Inject
    public RankBoardManager(ProxyRankManager rankManager) {
        this.rankManager = rankManager;
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {
        Initiator.super.close();
    }
}
