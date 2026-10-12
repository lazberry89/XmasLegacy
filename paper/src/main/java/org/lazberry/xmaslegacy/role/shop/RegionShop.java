package org.lazberry.xmaslegacy.role.shop;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.region.Region;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.*;

import java.util.Arrays;
import java.util.Objects;

@Registry.Exclude(type = ServerType.LOBBY)
public class RegionShop {
    public static final NamespacedKey key = KeyUtils.get("region_trader");
    private final RoleShopManager roleShopManager;

    @Inject
    public RegionShop(RoleShopManager roleShopManager) {
        this.roleShopManager = roleShopManager;
    }

    public static ItemStack material() {
        return ItemBuilder.of(XmasLegacy.getInstance(), Material.VILLAGER_SPAWN_EGG)
                .name(ColorUtils.chat(ColorUtils.GOLD + ColorUtils.BOLD + "개인상인 스폰알"))
                .lore(
                        ColorUtils.chat(ColorUtils.GRAY + "내 구역에 직업 상인을 배치할 수 있습니다."),
                        ColorUtils.chat(ColorUtils.GRAY + "우클릭하여 스폰해보세요!")
                )
                .hideAllFlags()
                .setTag(key, true)
                .setGlint(true)
                .setMaxStackSize(1)
                .build();
    }

    public boolean hasAnotherTrader(@Nullable Region region) {
        if (region == null) return false;

        var chunk = region.getChunk();
        if (chunk == null) return false;

        var entities = chunk.getEntities();
        if (entities.length == 0) return false;

        long count = Arrays.stream(entities)
                .filter(Entity::isValid)
                .filter(Villager.class::isInstance)
                .filter(e -> KeyUtils.hasKey(e, key))
                .count();
        return count >= 1;
    }

    public boolean notYourRegion(Player p, Region region) {
        return !Objects.equals(p.getUniqueId(), region.getOwner());
    }

    public boolean spawnTrader(Location loc, ServerRoles role) {
        if (role == ServerRoles.BLACKSMITH || role == ServerRoles.USER) return false;

        var profession = switch (role) {
            case FARMER -> Villager.Profession.FARMER;
            case MINER -> Villager.Profession.ARMORER;
            case HUNTER -> Villager.Profession.LEATHERWORKER;
            case FISHERMAN -> Villager.Profession.FISHERMAN;
            default -> null;
        };
        if (profession == null) return false;

        loc.getWorld().spawn(loc, Villager.class, v -> {
            v.setProfession(profession);
            v.setVillagerType(Villager.Type.SNOW);
            v.setVillagerLevel(5);
            v.customName(ColorUtils.chat(ColorUtils.GOLD + ColorUtils.BOLD + role.getKor() + " 상인"));
            v.setCustomNameVisible(true);
            v.setInvulnerable(true);
            v.setRemoveWhenFarAway(false);

            var speedAttr = v.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(0.0);
            }
            v.setCollidable(false);

            GlowUtils.glow(v, NamedTextColor.GOLD);
            KeyUtils.set(v, key, role.name());
        });
        return true;
    }

    public @Nullable ServerRoles getRoleByTrader(Entity entity) {
        if (entity == null) return null;
        if (!(entity instanceof Villager villager)) return null;

        return ParseEnum.of(ServerRoles.class).parse(KeyUtils.get(villager, key, PersistentDataType.STRING));
    }
}
