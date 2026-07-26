package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Sniper;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;

@Roles
@Registry.Exclude(type = ServerType.LOBBY)
public class Sniper extends AbstractSecondRole {
    private final @NotNull Container container;

    public static class Container implements RoleContainer {
        public final @NotNull XmasLegacy plugin = XmasLegacy.getInstance();
        public final @NotNull Map<UUID, BulletType> reloaded = new HashMap<>();
        public final @NotNull Set<UUID> isReloading = new HashSet<>();
        public final @NotNull Set<UUID> magicalBullet = new HashSet<>();
        public final @NotNull Map<UUID, BulletType> lastHitRecord = new HashMap<>();
        private final @NotNull Role role;

        Container(@NotNull SecondaryRoles role) {
            this.role = role;
        }

        public @Nullable BulletType getLastHitType(@NotNull Entity entity) {
            return lastHitRecord.get(entity.getUniqueId());
        }
        public ItemStack Gun(@Nullable Player p) {
            return ItemBuilder.of(plugin, Material.CROSSBOW)
                    .setName(ColorUtils.chat("&4&l인터셉터"))
                    .setLore(ColorUtils.chat(p == null || reloaded.get(p.getUniqueId()) == null ? "&7장전되지 않음" : "&6장전됨( " + reloaded.get(p.getUniqueId()).name() + " )"))
                    .setTag("role_id", role.name())
                    .setUnbreakable()
                    .build().clone();
        }
        public @Nullable BulletType getReloaded(@NotNull UUID uuid) {
            return reloaded.get(uuid);
        }
        public void replaceSnipe(@NotNull Player p) {
            Inventory inv = p.getInventory();
            ItemStack[] contents = inv.getStorageContents();

            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];

                if (item == null || item.getType().isAir()) continue;
                if (item.getType() != Material.CROSSBOW) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey roleKey = KeyUtils.get("role_id");

                if (!container.has(roleKey, PersistentDataType.STRING)) continue;
                String key = container.get(roleKey, PersistentDataType.STRING);

                if (key != null && key.equals("sniper")) {
                    inv.setItem(i, Gun(p));
                    return;
                }
            }
        }
    }

    public Sniper() {
        super(SecondaryRoles.SNIPER);
        this.container = new Container(getRole());
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.SNIPE), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.MAGIC_BULLET), container, 30);
    }

    public void fire(@NotNull Player shooter) {
        ItemStack tool = shooter.getInventory().getItemInMainHand();
        if (SkillManager.INSTANCE.get(SecondarySkillSet.FIRE_BULLET).execute(shooter, container)) {
            shooter.setCooldown(tool, 10);
        }
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return container.Gun(null);
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HELMET)
                .setName(ColorUtils.chat("&7&l3뚝"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .hideAllFlags()
                .build().clone();
    }
}
