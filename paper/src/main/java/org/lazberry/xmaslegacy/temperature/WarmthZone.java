package org.lazberry.xmaslegacy.temperature;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WarmthZone {
    @EqualsAndHashCode.Include
    private final long id;
    private final World world;
    private final WarmthLevel level;

    private double additionalChance;
    private double maxRegeneration;
    private boolean showParticle = true;
    private boolean regenerate = false;

    public WarmthZone(Chunk chunk, WarmthLevel level, double additionalChance, double maxRegeneration) {
        this.id = chunk.getChunkKey();
        this.world = chunk.getWorld();
        this.level = level;
        this.additionalChance = additionalChance;
        this.maxRegeneration = maxRegeneration;
    }

    public boolean isInside(Location loc) {
        return Objects.equals(loc.getChunk(), loc.getWorld().getChunkAt(id));
    }

    public Chunk getChunk() {
        return world.getChunkAt(id);
    }

    private Collection<Player> getValidPlayers(Collection<Player> targets) {
        return targets.stream()
                .filter(Objects::nonNull)
                .filter(Player::isValid)
                .filter(Player::isOnline)
                .toList();
    }

    public void applyWarmthEffect(UserManager userManager) {
        var entities = getChunk().getEntities();
        if (entities.length == 0) return;

        var players = Arrays.stream(entities)
                .filter(Entity::isValid)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(Player::isValid)
                .toList();
        players.forEach(p -> {
            User user = userManager.getUser(p.getUniqueId());
            if (user != null) increaseUserIcingState(user);
        });
        applyRegeneration(players);
    }

    public void increaseUserIcingState(User user) {
        user.addIcingState(level.getAdditionalWarmth());

        var player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null && player.isOnline() && player.isValid()) {
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 7, 0.5, 0.5, 0.5, 0.01);
        }
    }

    public void applyRegeneration(Collection<Player> targets) {
        Collection<Player> validPlayers = getValidPlayers(targets);
        if (validPlayers.isEmpty()) return;

        int size = validPlayers.size();
        double baseRegen = BigDecimal.valueOf(maxRegeneration / size)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        boolean isBonus = ThreadLocalRandom.current().nextDouble() < additionalChance;
        double additional = isBonus ? level.getAdditionalRegeneration() : 0.0;
        double totalRegen = baseRegen + additional;

        validPlayers.forEach(p -> {
            p.heal(totalRegen, EntityRegainHealthEvent.RegainReason.CUSTOM);
            String bonusText = isBonus ? String.format(" &a(+%.2f)", additional) : "";
            p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 제한 회복: &6+" + baseRegen + bonusText));
        });
    }
}
