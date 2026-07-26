package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Guardian;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.GUARDIAN;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.TARGET_GUARD;

@Skill(type = PlayerSkills.TARGET_GUARD)
@Registry.Exclude(type = ServerType.LOBBY)
public class TargetGuard implements Skills<Guardian.Container>, UsingEnergy {
	private final @NotNull PartyManager pm;

	@Inject
	public TargetGuard(@NotNull PartyManager pm) {
		this.pm = pm;
	}

	@Override
    public boolean execute(@NotNull Player caster, @NotNull Guardian.@NotNull Container container) {
        LivingEntity target = container.targetMap.get(caster);
        ItemStack tool = caster.getInventory().getItemInMainHand();
        if (target == null) {
            InfoUtils.error(caster, "연결된 타겟이 없습니다!");
            return false;
        }
        if (container.activeSkill.contains(caster.getUniqueId())) {
            container.activeSkill.remove(caster.getUniqueId());
            caster.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
            caster.setCooldown(tool, container.cooldown1);
            return false;
        }
        if (!consumeEnergy(caster, 3)) return false;

        container.activeSkill.add(caster.getUniqueId());
        caster.sendActionBar(ColorUtils.chat("&a스킬 활성화"));
        boolean isAlly = target instanceof Player t && pm.isParty(caster.getUniqueId(), t.getUniqueId());

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!container.activeSkill.contains(caster.getUniqueId())
                        || !container.targetMap.containsKey(caster)
                        || !target.isValid()
                        || !caster.isOnline()) {
                    container.activeSkill.remove(caster.getUniqueId());
                    if (isAlly) {
                        target.removePotionEffect(PotionEffectType.RESISTANCE);
                        target.removePotionEffect(PotionEffectType.REGENERATION);
                    } else {
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                    }
                    caster.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                    this.cancel();
                    caster.setCooldown(tool, container.cooldown1);
                    return;
                }


                if (ticks % 20 == 0) {
                    if (ticks >= 80) {
                        container.activeSkill.remove(caster.getUniqueId());
                        this.cancel();
                        caster.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                        caster.setCooldown(tool, container.cooldown1);
                        return;
                    }
                    if (!isAlly) {
                        target.damage(3, caster);
                    }
                    if (!consumeEnergy(caster, 2)) {
                        container.activeSkill.remove(caster.getUniqueId());
                        this.cancel();
                        InfoUtils.error(caster, "에너지가 모두 소모되었습니다.");
                        caster.sendActionBar(ColorUtils.chat("&c스킬 비활성화"));
                        caster.setCooldown(tool, container.cooldown1);
                        return;
                    }
                }

                if (isAlly) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2, 1, true, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 1, true, false, false));
                } else {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, 1, true, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2, 1, true, false, false));
                }
                ticks++;
            }
        }.runTaskTimer(container.plugin, 0L, 1L);
        return false;
    }

    @Override
    public @NotNull SkillSet type() {
        return TARGET_GUARD;
    }

    @Override
    public @NotNull Role role() {
        return GUARDIAN;
    }
}
