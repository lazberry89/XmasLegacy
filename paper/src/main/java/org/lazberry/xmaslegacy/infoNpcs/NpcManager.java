package org.lazberry.xmaslegacy.infoNpcs;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;

@Registry.Include(type = ServerType.GLOBAL)
public class NpcManager {
    private final @NotNull Map<NpcType, AbstractNpc> npcMap = new HashMap<>();

    public NpcManager() {
        this.npcMap.put(NpcType.MAIN, new MainNpc());
		this.npcMap.put(NpcType.ROLE, new CenterNpc());
		this.npcMap.put(NpcType.LIBRARIAN, new LibrarianNpc());
		this.npcMap.put(NpcType.COSMETIC, new CosmeticNpc());
    }

    @SuppressWarnings("unchecked")
    public <A extends AbstractNpc> A getNpcInstance(@NotNull NpcType type) {
        return (A) this.npcMap.get(type);
    }
}
