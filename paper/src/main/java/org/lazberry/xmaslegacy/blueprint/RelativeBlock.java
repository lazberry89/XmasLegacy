package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@Getter
@ConsumableClass
public class RelativeBlock {
    private final int x;
    private final int y;
    private final int z;
    private final Material material;
    private final String blockDataString;

    public RelativeBlock(int x, int y, int z, Material material, BlockData data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.blockDataString = data.getAsString();
    }

    public BlockData getBlockData() {
        return Bukkit.createBlockData(blockDataString);
    }
}
