package org.lazberry.xmaslegacy.RoleManagers;

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
import org.lazberry.xmaslegacy.Emblems.Emblem;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.PlayerSkillUseEvent;
import org.lazberry.xmaslegacy.SkillEffectManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface RoleClass {
	@NotNull Map<UUID, Integer> dashCount = new HashMap<>();

	void useFirstSkill(@NotNull Player p);
	void useSecondSkill(@NotNull Player p);

	default void useDash(@NotNull Player p, @NotNull Role role) {
		UUID uuid = p.getUniqueId();

		dashCount.putIfAbsent(uuid, role.getDashCount());

		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().isAir()) return;

		if (p.getCooldown(item) == 0 && dashCount.get(uuid) <= 0) {
			dashCount.put(uuid, role.getDashCount());
		}

		int count = dashCount.get(uuid);
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

		int nextCount = count - 1;
		dashCount.put(uuid, nextCount);

		if (nextCount <= 0) p.setCooldown(item, 20 * 60);
		else p.setCooldown(item, 10);
	}

	@CheckReturnValue
	default boolean isSkillCancelled(@NotNull Player p, @NotNull Emblem emblem, @NotNull EmblemType emblemType) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, emblemType);
		Bukkit.getPluginManager().callEvent(skillUse);

		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return true;

		if (p.getCooldown(tool) > 0) {
			p.sendActionBar(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return true;
		}
		return skillUse.isCancelled();
	}

	default <R extends RoleContainer> void handleSkill(
			@NotNull Player caster,
			@NotNull Emblem emblem,
			@NotNull EmblemType emblemType,
			@NotNull Skills<R> skill,
			R container,
			int tick) {
		if (isSkillCancelled(caster, emblem, emblemType)) return;
		ItemStack tool = caster.getInventory().getItemInMainHand();
		if (skill.execute(caster, container))
			caster.setCooldown(tool, tick);
	}
}
