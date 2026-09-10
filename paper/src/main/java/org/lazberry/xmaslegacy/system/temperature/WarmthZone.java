package org.lazberry.xmaslegacy.system.temperature;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.Axiom;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WarmthZone {
    @EqualsAndHashCode.Include
    private final String id;
    private final Location loc1;
    private final Location loc2;
    private final WarmthLevel level;
    private final double additionalChance;
    private double maxRegeneration;
    private boolean showParticle = true;
    private boolean regenerate = false;

    public WarmthZone(String id, Location loc1, Location loc2, WarmthLevel level, double additionalChance, double maxRegeneration) {
        this.id = id;
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.level = level;
        this.additionalChance = additionalChance;
        this.maxRegeneration = maxRegeneration;
    }

    public boolean isInside(Location loc) {
        return Axiom.isInBoundingBox(loc, loc1, loc2);
    }

    private Collection<Player> getValidPlayers(Collection<Player> targets) {
        return targets.stream()
                .filter(Objects::nonNull)
                .filter(Player::isValid)
                .filter(Player::isOnline)
                .toList();
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
