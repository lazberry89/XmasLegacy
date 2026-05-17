package xmasLegacy.SecondaryRoleManager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkill;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Defender extends AbstractSecondRole {
	private final SkillEffectManager SEM;
	private final Map<UUID, SecondarySkill> currentSkill = new HashMap<>();
	public SecondarySkill getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), SecondarySkill.MAGNETIC_FIELD);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Defender(XmasLegacy plugin) {
		super(plugin);
		this.SEM = plugin.SEM;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		Location loc = p.getLocation();
		p.getNearbyEntities(5, 5, 5).forEach(e -> {
			if (e instanceof LivingEntity target && !target.equals(p)) {
				Location targetLoc = target.getLocation();
				Vector direction = loc.toVector().subtract(targetLoc.toVector());

				direction.setY(0.5);

				Vector velocity = direction.normalize().multiply(2);
				target.setVelocity(velocity);
				SEM.drawLine(loc, targetLoc, Particle.SOUL_FIRE_FLAME, 0.1, false, false, 3, p);
				p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.3f);
			}
		});
		p.setCooldown(tool, 20);
	}

	@Override
	public void useSecondSkill(Player p) {

	}

	@Override
	public void usePassive(Player p) {

	}
}
