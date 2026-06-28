package xmaslegacy.SavingLocation;

import org.jetbrains.annotations.NotNull;
import xmaslegacy.PluginUtils.Initializer.ServerInitializer;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.SavingLocation.Lobby.LobbyManager;
import xmaslegacy.XmasLegacy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public enum SpawnRepository {
    INSTANCE;

    private final @NotNull Map<DestinationType, SavedLocation> spawnMap = new EnumMap<>(DestinationType.class);

    SpawnRepository() {
        if (ServerInitializer.getServerType(XmasLegacy.getInstance()).equals(ServerType.LOBBY))
            this.spawnMap.put(DestinationType.LOBBY, new LobbyManager());
        else {
            this.spawnMap.put(DestinationType.FROZEN_PORT, new PortVillageManager());
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
