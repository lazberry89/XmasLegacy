package xmasLegacy.SecondaryRoleManager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.SecondarySkill;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode, unused")
public class Defender extends AbstractSecondRole {
	private final SkillEffectManager SEM;
	private final PartyManager PM;
	private final Map<UUID, SecondarySkill> currentSkill = new HashMap<>();
	public SecondarySkill getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), SecondarySkill.MAGNETIC_FIELD);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Defender(XmasLegacy plugin) {
		super(plugin);
		this.SEM = plugin.SEM;
		this.PM = plugin.PM;
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
				if (!PM.isParty(p.getUniqueId(), target.getUniqueId())) {
					Location targetLoc = target.getLocation();
					Vector direction = loc.toVector().subtract(targetLoc.toVector());

					direction.setY(0.5);

					Vector velocity = direction.normalize().multiply(2);
					target.setVelocity(velocity);
					SEM.drawLine(loc, targetLoc, Particle.SOUL_FIRE_FLAME, 0.1, false, false, 3, p);
					p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.3f);
				}
			}
		});
		p.setCooldown(tool, 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;

		p.getNearbyEntities(4, 4, 4).stream()
				.filter(e -> e != p && e instanceof LivingEntity)
				.filter(e -> !PM.isParty(p.getUniqueId(), e.getUniqueId()))
				.map(e -> (LivingEntity) e)
				.forEach(le -> {
					le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 20, true, false, false));
					le.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, le.getLocation().clone().add(0, 1, 0), 5, 0.1, 0.1, 0.1, 0.01);
				});
		p.getWorld().playSound(p, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 0.8f);
		p.setCooldown(tool, 20);
	}

	@Override
	public void usePassive(Player p) {

	}

	@Override
	public @NonNull ItemStack roleWeapon() {
		return null;
	}

	@Override
	public @NonNull ItemStack roleArmor() {
		return null;
	}
}
