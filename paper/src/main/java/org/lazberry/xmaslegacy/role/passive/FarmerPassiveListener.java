package org.lazberry.xmaslegacy.role.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.role.general.RoleManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

@Listeners
@Registry.Include(type = ServerType.WILD)
public class FarmerPassiveListener extends PassiveListeners implements Listener {
    private final RoleManager rm;

    @Inject
    public FarmerPassiveListener(UserManager um, RoleManager rm) {
        super(um);
        this.rm = rm;
    }

    @EventHandler
    public void whenCropBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        Player p = e.getPlayer();

        super.canUsePassive(p, u -> {
            if (broken.getType() == Material.FIRE || broken.getType() == Material.SOUL_FIRE) return;

            if (broken.getBlockData() instanceof Ageable crop) {
                if (crop.getAge() != crop.getMaximumAge()) return;

                int extraAmount = ThreadLocalRandom.current()
                        .nextInt(rm.farmer().getAdditionalDropsMin(), rm.farmer().getAdditionalDropsMax());
                Collection<ItemStack> drops = broken.getDrops(p.getInventory().getItemInMainHand());

                Location dropLoc = broken.getLocation();

                for (ItemStack drop : drops) {
                    if (drop.getType().name().endsWith("_SEEDS")) continue;
                    ItemStack extraDrop = drop.clone();
                    extraDrop.setAmount(extraAmount);

                    dropLoc.getWorld().dropItemNaturally(dropLoc, extraDrop);
                    return;
                }
            }
        });
    }
}
