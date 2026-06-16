package xmaslegacy.RoleManagers.SecondaryRoleManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Emblems.Emblem;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.PlayerSkillUseEvent;
import xmaslegacy.RoleManagers.UsingEnergy;
import xmaslegacy.XmasLegacy;

public abstract class AbstractSecondRole implements UsingEnergy {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull SecondaryRoles role;
	protected final @NotNull Emblem emblem;

	public AbstractSecondRole(@NotNull SecondaryRoles role) {
		this.plugin = XmasLegacy.getInstance();
		this.role = role;
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
	public abstract @NotNull ItemStack TargetEmblem();
	public abstract @NotNull ItemStack RangeEmblem();
}
