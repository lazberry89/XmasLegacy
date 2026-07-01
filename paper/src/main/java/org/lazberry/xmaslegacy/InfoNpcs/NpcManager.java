package org.lazberry.xmaslegacy.InfoNpcs;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum NpcManager {
    INSTANCE;

    private final @NotNull Map<NpcType, AbstractNpc> npcMap = new HashMap<>();

    NpcManager() {
        this.npcMap.put(NpcType.MAIN, new MainNpc());
		this.npcMap.put(NpcType.ROLE, new CenterNpc());
		this.npcMap.put(NpcType.WITCH, new WitchNpc());
		this.npcMap.put(NpcType.WANDER, new WanderingGrandpaNpc());
		this.npcMap.put(NpcType.LIBRARIAN, new LibrarianNpc());
		this.npcMap.put(NpcType.BOOK, new HidingBook());
		this.npcMap.put(NpcType.VILLAGER1, new Villager1());
		this.npcMap.put(NpcType.VILLAGER2, new Villager2());
		this.npcMap.put(NpcType.VILLAGER3, new Villager3());
		this.npcMap.put(NpcType.VILLAGER4, new Villager4());
		this.npcMap.put(NpcType.VILLAGER5, new Villager5());
		this.npcMap.put(NpcType.VILLAGER6, new Villager6());
		this.npcMap.put(NpcType.VILLAGER7, new Villager7());
		this.npcMap.put(NpcType.VILLAGER8, new Villager8());
		this.npcMap.put(NpcType.VILLAGER9, new Villager9());
    }

    @SuppressWarnings("unchecked")
    public <A extends AbstractNpc> A getNpcInstance(@NotNull NpcType type) {
        return (A) this.npcMap.get(type);
    }
}
