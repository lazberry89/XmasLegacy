package org.lazberry.xmaslegacy.prefix;

import org.lazberry.xmaslegacy.settings.ServerPrefix;

import java.sql.Timestamp;
import java.util.UUID;

public record PrefixStatus(ServerPrefix prefix, UUID owner, Timestamp acquiredAt) {
}
