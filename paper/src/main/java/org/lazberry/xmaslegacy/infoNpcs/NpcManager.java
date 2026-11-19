package org.lazberry.xmaslegacy.infoNpcs;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;

import java.util.HashMap;
import java.util.Map;

@Components.Include(type = ServerType.GLOBAL)
public class NpcManager {
    private final Map<NpcType, AbstractNpc> npcMap = new HashMap<>();

    public NpcManager() {
        this.npcMap.put(NpcType.MAIN, new MainNpc());
		this.npcMap.put(NpcType.ROLE, new CenterNpc());
		this.npcMap.put(NpcType.LIBRARIAN, new LibrarianNpc());
		this.npcMap.put(NpcType.COSMETIC, new CosmeticNpc());
		this.npcMap.put(NpcType.SELECT, new RoleSelectNpc());
    }

    @SuppressWarnings("unchecked")
    public <A extends AbstractNpc> A getNpcInstance(NpcType type) {
        return (A) this.npcMap.get(type);
    }
}
