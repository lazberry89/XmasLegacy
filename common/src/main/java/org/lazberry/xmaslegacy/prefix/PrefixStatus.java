package org.lazberry.xmaslegacy.prefix;

import org.lazberry.xmaslegacy.settings.ServerPrefix;

import java.util.UUID;

public record PrefixStatus(ServerPrefix prefix, UUID owner, long givenAt) {
}
