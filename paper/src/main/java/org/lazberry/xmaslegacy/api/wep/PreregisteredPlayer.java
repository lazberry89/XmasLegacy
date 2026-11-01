package org.lazberry.xmaslegacy.api.wep;

import java.util.UUID;

public record PreregisteredPlayer(UUID uuid, String name, long registeredAt) {
}
