package org.lazberry.xmaslegacy.SavingLocation;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.Utils.ServerUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Registry.Include(type = ServerType.HUNTING)
public class SpawnRepository {
    private final @NotNull Map<DestinationType, SavedLocation> spawnMap = new EnumMap<>(DestinationType.class);

    public SpawnRepository() {
        if (ServerUtils.getServerType(XmasLegacy.getInstance()).equals(ServerType.LOBBY))
            this.spawnMap.put(DestinationType.LOBBY, new LobbyManager());
        else {
            this.spawnMap.put(DestinationType.PORT, new PortVillageManager());
            this.spawnMap.put(DestinationType.MAIN, new MainSpawnManager());
        }
    }

    public @NotNull DestinationType[] availableTypes() {
        return spawnMap.keySet().toArray(DestinationType[]::new);
    }

    @SuppressWarnings("unchecked")
    public <S extends SavedLocation> @NotNull S get(@NotNull DestinationType type) {
        var value = this.spawnMap.get(type);
        return (S) Objects.requireNonNull(value, String.format("You can't use that type in this server! (%s)", type));
    }
}
