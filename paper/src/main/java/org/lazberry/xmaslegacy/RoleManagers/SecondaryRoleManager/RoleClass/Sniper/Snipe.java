package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Sniper;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.*;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Skill(type = PlayerSkills.SNIPE)
@Registry.Exclude(type = ServerType.LOBBY)
public class Snipe implements Skills<Sniper.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Sniper.@NotNull Container container) {
        UUID uuid = caster.getUniqueId();
        if (container.isReloading.contains(uuid)) {
            InfoUtils.warn(caster, "이미 장전중입니다!");
            return false;
        }
        if (!consumeEnergy(caster, 3)) return false;
        BulletType bullet = selectBullet();

        caster.sendActionBar(ColorUtils.chat("&a장전중..."));
        container.isReloading.add(caster.getUniqueId());
        caster.getWorld().playSound(caster, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(container.plugin, () -> {
            try {
                if (caster.isOnline() && caster.isValid()) {
                    container.reloaded.put(uuid, bullet);
                    caster.getWorld().playSound(caster, Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
                    caster.sendActionBar(ColorUtils.chat(Alert.GREEN + " 장전완료 (탄환 : &b" + bullet.name() + "&f)"));
                    container.replaceSnipe(caster);
                }
            } finally {
                container.isReloading.remove(uuid);
            }
        }, (long) 25 + 30);
        return true;
    }

    private @NotNull BulletType selectBullet() {
        int num = ThreadLocalRandom.current().nextInt(0, 3);
        return BulletType.getType(num);
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.SNIPE;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.SNIPER;
    }
}
