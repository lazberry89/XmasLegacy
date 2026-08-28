package org.lazberry.xmaslegacy.collectors.drop;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlayerBreakContainerEvent extends Event {
    private static final HandlerList handler = new HandlerList();
    private final List<LivingEntity> affectedEntities = new ArrayList<>();
    private final NamespacedKey key;
    private final Session session;
    private final Player player;
    private final Location location;

    public PlayerBreakContainerEvent(Session session, Player player, Location location) {
        this.session = session;
        this.key = KeyUtils.get(session.getDifficulty().name() + "_hunter");
        this.player = player;
        this.location = location;
        affectedEntities.addAll(location.getNearbyEntitiesByType(LivingEntity.class, 7, 7,
                e -> e.getPersistentDataContainer().has(key)));
    }

    public <T extends LivingEntity> boolean isAffected(T entity) {
        return affectedEntities.contains(entity);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }
    public static HandlerList getHandlerList() {
        return handler;
    }
}
