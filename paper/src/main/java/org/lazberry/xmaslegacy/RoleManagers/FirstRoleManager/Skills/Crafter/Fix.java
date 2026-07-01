package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Crafter;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class Fix implements Skills<Crafter.Container> {
	@Override
	public void execute(@NotNull Player caster, @NotNull Crafter.Container cnt) {

		double percent = cnt.first_skill_repair_percent();
		int repairAmount = (int) (cnt.item().getType().getMaxDurability() * percent);
		int newDamage = Math.max(0, cnt.current_damage() - repairAmount);

		cnt.damageable().setDamage(newDamage);
		cnt.item().setItemMeta(cnt.damageable());
		cnt.item_entity().setItemStack(cnt.item());

		// 5. 피드백
		caster.sendMessage(ColorUtils.chat(Alert.GREEN + " 성공적으로 수리했습니다! &7(수리량: " + repairAmount + ")"));
		caster.playSound(caster.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.FIX;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.CRAFTER;
	}
}
