package org.lazberry.xmaslegacy.enchant.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@SuppressWarnings("UnstableApiUsage")
@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantItemListener implements Listener {
    private final XmasLegacy plugin;

    @Inject
    public EnchantItemListener(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void addFlagWhenPlayerHasTotems(PlayerEnchantEvent e) {
        var player = e.getPlayer();
        if (InventoryHelper.hasItem(player, EnchantMaterial.DowngradeProtector(), 1)) {
            e.setPreventDowngrade(true);
        }
        if (InventoryHelper.hasItem(player, EnchantMaterial.BreakProtector(), 1)) {
            e.setPreventBreak(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void useDowngradeProtector(EnchantDowngradePreventEvent e) {
        if (e.isCancelled()) return;
        var player = e.getPlayer();

        if (InventoryHelper.removeItem(player, EnchantMaterial.DowngradeProtector(), 1)) {
            World world = player.getWorld();
            Location loc = player.getLocation();
            world.playSound(player, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, loc.clone().add(0, 1, 0), 7, 0.15, 0.15, 0.15, 0.01);
            InfoUtils.info(player, "하락방지권이 사용되어 무기 등급이 유지됩니다. (현재: &6{}등급&f)", e.getCurrentLevel());
        } else {
            e.setCancelled(true);
            InfoUtils.warn(player, "강화도중 보호아이템이 인식되지 않아 강화보호가 작용하지 않습니다.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void useBreakProtector(EnchantBrokenPreventedEvent e) {
        if (e.isCancelled()) return;
        var player = e.getPlayer();

        if (InventoryHelper.removeItem(player, EnchantMaterial.BreakProtector(), 1)) {
            World world = player.getWorld();
            Location loc = player.getLocation().clone();
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    loc.add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.01);
            world.playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

            var totem = e.getItem().clone();
            totem.setData(DataComponentTypes.DEATH_PROTECTION, DeathProtection.deathProtection());
            var savedItem = player.getInventory().getItemInMainHand().clone();
            var inv = player.getInventory();

            inv.setItemInMainHand(totem);
            player.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
				totem.setAmount(0);
                inv.setItemInMainHand(savedItem);
            }, 1L);
            InfoUtils.info(player, "부활의 표식을 사용하여 아이템이 복구됨.");
        } else {
            e.setCancelled(true);
            InfoUtils.warn(player, "강화도중 보호아이템이 인식되지 않아 강화보호가 작용하지 않습니다.");
        }
    }
}
