package org.lazberry.xmaslegacy.wep;

import java.util.UUID;

public record PreregisteredPlayer(UUID uuid, String name, long registeredAt) {
}
