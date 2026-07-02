package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Crafter;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class Fix implements Skills<Crafter.Container>, UsingEnergy {

	@Override
	public void execute(@NotNull Player caster, @NotNull Crafter.Container cnt) {
		if (!consumeEnergy(caster, cnt.first_skill_hunger_cost())) return;

		Entity target = caster.getTargetEntity(cnt.first_skill_raytrace_range(), false);
		if (target == null) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 대상이 없습니다!"));
			return;
		}
		if (!(target instanceof Item itemEntity)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 아이템(드롭된 아이템)을 조준해주세요!"));
			return;
		}
		ItemStack itemStack = itemEntity.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();

		if (!(meta instanceof Damageable damageable)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 수 없는 아이템입니다!"));
			return;
		}

		int currentDamage = damageable.getDamage();
		if (currentDamage <= 0) {
			caster.sendMessage(ColorUtils.chat(Alert.YELLOW + " 이미 새 아이템입니다!"));
			return;
		}
		double percent = cnt.first_skill_repair_percent();
		int repairAmount = (int) (cnt.item().getType().getMaxDurability() * percent);
		int newDamage = Math.max(0, currentDamage - repairAmount);

		damageable.setDamage(newDamage);
		itemStack.setItemMeta(damageable);
		itemEntity.setItemStack(itemStack);

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
