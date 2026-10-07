package org.lazberry.xmaslegacy.enchant.material.shop;

import lombok.Getter;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Getter
@Registry.Exclude(type = ServerType.LOBBY)
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
