package org.lazberry.xmaslegacy.role.passive.listeners;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.enchant.listener.PlayerEnchantEvent;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.enchant.material.shop.EnchantShopManager;
import org.lazberry.xmaslegacy.exp.ExpManager;
import org.lazberry.xmaslegacy.role.general.RoleManager;
import org.lazberry.xmaslegacy.role.general.RoleTool;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class BlackSmithPassiveListener extends PassiveListeners implements Listener {
    private final RoleManager rm;
    private final ExpManager em;
    private final EnchantManager ecm;
    private final EnchantShopManager shopManager;

    @Inject
    public BlackSmithPassiveListener(UserManager um, RoleManager rm, ExpManager em, EnchantManager ecm, EnchantShopManager shopManager) {
        super(ServerRoles.BLACKSMITH, um);
        this.rm = rm;
        this.em = em;
        this.ecm = ecm;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void whenEnchantTool(PlayerEnchantEvent e) {
        if (e.isCancelled()) return;

        var player = e.getPlayer();
        int currentLevel = e.getCurrentLevel();

        ItemStack giveItem = EnchantMaterial.EssenceOfEnchant();
        giveItem.setAmount(currentLevel);

        InventoryHelper.giveItemOrDrop(player, giveItem);
        em.addRoleExp(player, 20);
        sendExpAlert(player, 20);
    }

    @EventHandler
    public void whenInteractWithTool(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;

        var item = e.getItem();
        if (item == null || item.getType().isAir()) return;

        var player = e.getPlayer();
        RoleTool.isTool(item, r ->
            canUsePassive(player, u -> {
                if (r == ServerRoles.BLACKSMITH) {
                    if (!player.isSneaking()) {
                        if (player.getCooldown(item) > 0) return;

                        ecm.openInterface(player);
                        player.setCooldown(item, 20 * 30);

                        var giveItem = EnchantMaterial.PrismFractal();
                        giveItem.setAmount(5);
                        InventoryHelper.giveItemOrDrop(player, giveItem);
                    } else {
                        player.openInventory(shopManager.getMaterialShop().getInventory());
                        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_FALL, 1.0f, 1.0f);
                    }
                }
            }));
    }
}
