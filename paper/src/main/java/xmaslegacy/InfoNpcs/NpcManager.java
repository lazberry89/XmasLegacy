package xmaslegacy.InfoNpcs;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum NpcManager {
    INSTANCE;

    private final @NotNull Map<NpcType, AbstractNpc> npcMap = new HashMap<>();

    NpcManager() {
        this.npcMap.put(NpcType.MAIN, new MainNpc());
    }

    @SuppressWarnings("unchecked")
    public <A extends AbstractNpc> A getNpcInstance(@NotNull NpcType type) {
        return (A) this.npcMap.get(type);
    }
}
