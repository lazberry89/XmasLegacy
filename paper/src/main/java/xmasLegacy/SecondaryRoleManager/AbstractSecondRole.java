package xmasLegacy.SecondaryRoleManager;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Emblems.Emblem;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.UsingEnergy;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode, FieldCanBeLocal, unused, BooleanMethodIsAlwaysConverted")
public abstract class AbstractSecondRole implements UsingEnergy {
	private final Map<UUID, Integer> dashCount = new HashMap<>();
	private final XmasLegacy plugin;
	private final SecondaryRoles role;
	protected final Emblem emblem;

	public AbstractSecondRole(SecondaryRoles role) {
		this.plugin = XmasLegacy.getInstance();
		this.role = role;
		this.emblem = new Emblem(role);
	}

	public abstract void useFirstSkill(Player p);
	public abstract void useSecondSkill(Player p);
	public abstract void usePassive(Player p);

	@Override
	public boolean consumeEnergy(Player player, int hungerCost) {
		int currentFood = player.getFoodLevel();

		if (currentFood < hungerCost) {
			player.sendMessage(ColorUtils.chat(Alert.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return false;
		}

		player.setFoodLevel(Math.max(0, currentFood - hungerCost));

		return true;
	}

	@Override
	public void useDash(Player p) {
		UUID uuid = p.getUniqueId();
		this.dashCount.putIfAbsent(uuid, role.getDashCount());
		int count = this.dashCount.getOrDefault(uuid, role.getDashCount());
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		if (count <= 0 || p.getCooldown(item) > 0) {
			p.sendActionBar(ColorUtils.chat(Alert.RED + " 대시 사용 불가"));
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}
		Vector vector = p.getLocation().getDirection();
		Vector velocity = vector.normalize().multiply(2.0);

		double finalY = velocity.getY();
		if (finalY > 1.2) {
			finalY = 1.2;
		} else if (finalY < -1.2) {
			finalY = -1.2;
		}
		velocity.setY(finalY);

		SkillEffectManager.getInstance().followParticle(p, Particle.END_ROD, 10);
		p.setVelocity(velocity);

		this.dashCount.put(uuid, count - 1);
		if (this.dashCount.getOrDefault(uuid, role.getDashCount()) == 0) {
			p.setCooldown(item, 20 * 60);
			this.dashCount.put(uuid, role.getDashCount());
		} else {
			p.setCooldown(item, 10);
		}
	}

	public XmasLegacy getPlugin() {
		return this.plugin;
	}

	public abstract @NotNull Role getRole();
	public abstract @NotNull ItemStack roleWeapon();
	public abstract @NotNull ItemStack roleArmor();
	public abstract @NotNull ItemStack TargetEmblem();
	public abstract @NotNull ItemStack RangeEmblem();
}
