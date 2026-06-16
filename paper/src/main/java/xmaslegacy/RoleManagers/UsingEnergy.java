package xmaslegacy.RoleManagers;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Emblems.Emblem;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.PlayerSkillUseEvent;
import xmaslegacy.SkillEffectManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public interface UsingEnergy {
	@NotNull Map<UUID, Integer> dashCount = new HashMap<>();
	default boolean consumeEnergy(@NotNull Player player, int hungerCost) {
		int currentFood = player.getFoodLevel();

		if (currentFood < hungerCost) {
			player.sendMessage(ColorUtils.chat(Alert.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return false;
		}

		player.setFoodLevel(Math.max(0, currentFood - hungerCost));

		return true;
	}
	default void useDash(@NotNull Player p, @NotNull Role role) {
		UUID uuid = p.getUniqueId();
		dashCount.putIfAbsent(uuid, role.getDashCount());
		int count = dashCount.getOrDefault(uuid, role.getDashCount());
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

		SkillEffectManager.INSTANCE.followParticle(p, Particle.END_ROD, 10);
		p.setVelocity(velocity);

		dashCount.put(uuid, count - 1);
		if (dashCount.getOrDefault(uuid, role.getDashCount()) == 0) {
			p.setCooldown(item, 20 * 60);
			dashCount.put(uuid, role.getDashCount());
		} else {
			p.setCooldown(item, 10);
		}
	}

	@CheckReturnValue
	default boolean isSkillCancelled(@NotNull Player p, @NotNull UsingEnergy usingEnergy, @NotNull Emblem emblem, @NotNull EmblemType emblemType) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, usingEnergy, emblem, emblemType);
		Bukkit.getPluginManager().callEvent(skillUse);

		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return true;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return true;
		}
		return skillUse.isCancelled();
	}
}
