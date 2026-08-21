package org.lazberry.xmaslegacy.role.passive.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.Roles.ServerRoles;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.exp.ExpManager;
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
    private final ExpManager em;

    @Inject
    public FarmerPassiveListener(UserManager um, RoleManager rm, ExpManager em) {
        super(ServerRoles.FARMER, um);
        this.rm = rm;
        this.em = em;
    }

    @EventHandler
    public void whenCropBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        Player p = e.getPlayer();

        super.canUsePassive(p, u -> {
            if (broken.getType() == Material.FIRE || broken.getType() == Material.SOUL_FIRE) return;

            if (broken.getBlockData() instanceof Ageable crop) {
                if (crop.getAge() != crop.getMaximumAge()) return;

                int minDrops = rm.farmer().getAdditionalDropsMin();
                int maxDrops = rm.farmer().getAdditionalDropsMax();
                int extraAmount = ThreadLocalRandom.current().nextInt(minDrops, maxDrops + 1);

                Collection<ItemStack> drops = broken.getDrops(p.getInventory().getItemInMainHand());
                Location dropLoc = broken.getLocation();

                for (ItemStack drop : drops) {
                    if (drop.getType().name().endsWith("_SEEDS")) continue;

                    ItemStack extraDrop = drop.clone();
                    extraDrop.setAmount(extraAmount);
                    dropLoc.getWorld().dropItemNaturally(dropLoc, extraDrop);
                    break;
                }

                int minExp = rm.farmer().getAdditionalExpMin();
                int maxExp = rm.farmer().getAdditionalExpMax();
                int expToGive = ThreadLocalRandom.current().nextInt(minExp, maxExp + 1);

                sendExpAlert(p, expToGive);
                em.addRoleExp(p, expToGive);
            }
        });
    }

    @EventHandler
    public void whenCropPlant(BlockPlaceEvent e) {
        Block placed = e.getBlockPlaced();
        Player p = e.getPlayer();

        super.canUsePassive(p, u -> {
            if (placed.getBlockData() instanceof Ageable ageable) {
                if (placed.getType() == Material.FIRE || placed.getType() == Material.SOUL_FIRE) return;

                if (ThreadLocalRandom.current().nextDouble() < rm.farmer().getPlantingExpChance()) {
                    int amount = rm.farmer().getPlantingExp();
                    em.addRoleExp(p, amount);
                    sendExpAlert(p, amount);
                }

                applyBonusGrowth(p, placed, ageable);
            }
        });
    }

    private void applyBonusGrowth(Player player, Block block, Ageable crop) {
        double bonusChance = rm.farmer().getBonusGrowthChance();

        if (ThreadLocalRandom.current().nextDouble() >= bonusChance) return;

        int maxAge = crop.getMaximumAge();
        double instantMaxChance = rm.farmer().getInstantMaxGrowthChance();

        if (ThreadLocalRandom.current().nextDouble() < instantMaxChance) {
            crop.setAge(maxAge);
        } else {
            int currentAge = crop.getAge();
            int minBonus = rm.farmer().getBonusGrowthMin();
            int maxBonus = rm.farmer().getBonusGrowthMax();
            int bonusAge = ThreadLocalRandom.current().nextInt(minBonus, maxBonus + 1);

            crop.setAge(Math.min(currentAge + bonusAge, maxAge));
        }

        block.setBlockData(crop);

        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.2, 0.2, 0.2);
        player.playSound(loc, Sound.ITEM_BONE_MEAL_USE, 0.5f, 1.2f);
    }

    @EventHandler
    public void onFarmlandTrample(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        if (clicked.getType() == Material.FARMLAND) {
            Player p = e.getPlayer();

            super.canUsePassive(p, u -> e.setCancelled(true));
        }
    }
}