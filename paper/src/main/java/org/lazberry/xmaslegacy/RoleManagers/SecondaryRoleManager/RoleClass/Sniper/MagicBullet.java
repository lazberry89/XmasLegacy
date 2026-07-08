package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Sniper;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.UUID;

@Skill(type = PlayerSkills.MAGIC_BULLET)
public class MagicBullet implements Skills<Sniper.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Sniper.@NotNull Container container) {
        UUID uuid = caster.getUniqueId();
        if (container.magicalBullet.contains(uuid)) {
            InfoUtils.error(caster, "이미 장전되어 있습니다.");
            return false;
        }
        if (container.isReloading.contains(caster.getUniqueId())) {
            InfoUtils.warn(caster, "이미 장전중입니다!");
            return false;
        }
        if (!consumeEnergy(caster, 3)) return false;
        caster.sendActionBar(ColorUtils.chat("&a장전중..."));
        container.isReloading.add(uuid);
        caster.getWorld().playSound(caster, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(container.plugin, () -> {
            BulletType type = BulletType.MAGICAL;
            container.magicalBullet.add(caster.getUniqueId());
            caster.getWorld().playSound(caster, Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
            caster.sendActionBar(ColorUtils.chat(Alert.GREEN + " 장전완료 (탄환 : &b" + type.name() + "&f)"));
            container.replaceSnipe(caster);
            container.isReloading.remove(uuid);
        }, (long) 60 + 30);
        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.MAGIC_BULLET;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.SNIPER;
    }
}
