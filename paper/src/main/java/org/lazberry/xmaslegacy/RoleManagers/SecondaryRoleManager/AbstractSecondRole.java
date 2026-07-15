package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Emblems.Emblem;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;

@Inject
public abstract class AbstractSecondRole implements RoleClass {
	private @Manager @NotNull XmasLegacy plugin;
	private final @NotNull SecondaryRoles role;
	private final @NotNull @Getter SkillManager skillManager;
	protected final @NotNull Emblem emblem;

	public AbstractSecondRole(@NotNull SecondaryRoles role) {
		this.role = role;
		this.skillManager = SkillManager.INSTANCE;
		this.emblem = new Emblem(role);
	}

	public abstract void useFirstSkill(@NotNull Player p);
	public abstract void useSecondSkill(@NotNull Player p);
	public abstract void usePassive(@NotNull Player p);

	public @NotNull XmasLegacy getPlugin() {
		return this.plugin;
	}

	public @NotNull SecondaryRoles getRole() {
		return this.role;
	}
	public abstract @NotNull ItemStack roleWeapon();
	public abstract @NotNull ItemStack roleArmor();
	public @NotNull ItemStack targetEmblem() {
		return this.emblem.getTargetEmblem();
	}
	public @NotNull ItemStack rangeEmblem() {
		return this.emblem.getRangeEmblem();
	}
}
