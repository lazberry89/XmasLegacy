package org.lazberry.xmaslegacy.teleporter.logic;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.lazberry.xmaslegacy.utils.Axiom;

import java.util.function.Consumer;

@Getter
public class Teleporter {
    private final Location loc1;
    private final Location loc2;
    private final Color color;
    private final Particle.DustOptions option;

    public Teleporter(Location loc1, Location loc2, Color color) {
        this.loc1 = Axiom.toExactCenterLocation(loc1);
        this.loc2 = Axiom.toExactCenterLocation(loc2);
        this.color = color;
        this.option = new Particle.DustOptions(color, 0.6f);
    }

    public boolean isInside(Location loc) {
        return Axiom.isInBoundingBox(loc, loc1, loc2);
    }

    public void forEachLocation(Consumer<Location> action) {
        Axiom.loopArea(loc1, loc2, action);
    }

    public void forEachBlock(Consumer<Block> action) {
        forEachLocation(loc -> action.accept(loc.getBlock()));
    }

    public void playParticleEffect() {
        forEachBlock(b ->
            b.getWorld().spawnParticle(Particle.DUST, b.getLocation(),
                    3, 0.3, 0.3, 0.3, 0.01, option));
    }
}
