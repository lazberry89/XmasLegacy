package org.lazberry.xmaslegacy.api;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Registry.Exclude(type = ServerType.LOBBY)
public class ProductRecordRepository {
    private final String url = "jdbc:mysql://localhost:3306/xmas_legacy?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private final String user = "husksync";
    private final String password = "husksync";
}
