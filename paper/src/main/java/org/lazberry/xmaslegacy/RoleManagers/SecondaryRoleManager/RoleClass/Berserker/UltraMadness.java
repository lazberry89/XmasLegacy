package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Berserker;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.BERSERKER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.ULTRA_MADNESS;

@Skill(type = PlayerSkills.ULTRA_MADNESS)
public class UltraMadness implements Skills<Berserker.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Berserker.Container container) {
        if (!(consumeEnergy(caster, 3))) return false;
        var uuid = caster.getUniqueId();
        GlowUtils.glow(caster, NamedTextColor.DARK_RED);
        caster.getWorld().playSound(caster, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
        SkillEffectManager.INSTANCE.setImmuneToKnockback(uuid, true);
        SkillEffectManager.INSTANCE.setImmuneToDebuff(uuid, true);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false, false));

        caster.sendActionBar(ColorUtils.chat("&7&l[&e&l 디버프 무시됨 &7&l]"));
        Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
            SkillEffectManager.INSTANCE.setImmuneToKnockback(uuid, false);
            SkillEffectManager.INSTANCE.setImmuneToDebuff(uuid, false);
            caster.getWorld().playSound(caster, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.6f);
        }, 60L);
        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return ULTRA_MADNESS;
    }

    @Override
    public @NotNull Role role() {
        return BERSERKER;
    }
}
