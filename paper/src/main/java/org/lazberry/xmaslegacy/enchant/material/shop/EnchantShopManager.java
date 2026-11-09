package org.lazberry.xmaslegacy.enchant.material.shop;

import lombok.Getter;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

@Getter
@Components.Exclude(type = ServerType.LOBBY)
public class EnchantShopManager {
    private final XmasLegacy plugin;
    private final EnchantMaterialShop materialShop;
    private final ProtectorShop protectorShop;

    @Inject
    public EnchantShopManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.materialShop = new EnchantMaterialShop(plugin);
        this.protectorShop = new ProtectorShop();
    }
}
