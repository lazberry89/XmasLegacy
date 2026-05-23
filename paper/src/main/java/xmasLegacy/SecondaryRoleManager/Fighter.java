package xmasLegacy.SecondaryRoleManager;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.SecondarySkill;
import xmasLegacy.Emblems.Emblem;
import xmasLegacy.XmasLegacy;

import java.util.*;

@SuppressWarnings("DuplicatedCode, unused")
public class Fighter extends AbstractSecondRole {
    private final Map<UUID, SecondarySkill> currentSkill = new HashMap<>();
    public SecondarySkill getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), SecondarySkill.COUNTER);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
    public final Set<UUID> counter = new HashSet<>();
    private final XmasLegacy plugin;
    private final PartyManager pm;
	private final Emblem emblem;

    public Fighter() {
        super(SecondaryRoles.FIGHTER);
        this.plugin = XmasLegacy.getInstance();
        this.pm = PartyManager.getInstance();
		this.emblem = new Emblem(SecondaryRoles.FIGHTER);
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
        UUID uuid = p.getUniqueId();
        if (counter.contains(uuid)) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 카운터를 사용중입니다."));
            return;
        }
        counter.add(uuid);
        p.getWorld().playSound(p, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.3f);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            counter.remove(uuid), 30L);

        p.setCooldown(tool, 40);
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
        if (!(p.getTargetEntity(2, false) instanceof LivingEntity target)) return;
        Vector vector = p.getLocation().getDirection();
        p.setVelocity(vector.multiply(2.5));
        Location startLoc = p.getLocation().add(0, 1, 0);
        Vector dir = vector.clone().normalize();

        Vector ortho;
        if (Math.abs(dir.getY()) > 0.9) {
            ortho = new Vector(1, 0, 0); // 수직으로 하늘을 볼 때 예외 처리
        } else {
            ortho = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
        }

        double radius = 0.5;
        double spiralTightness = 3.5;
        ortho.multiply(radius);
        Particle.DustOptions option = new Particle.DustOptions(Color.BLUE, 1.1f);
        Particle.DustTransition trs = new Particle.DustTransition(Color.BLUE, Color.AQUA, 1.1f);

        for (double d = 0; d < 6.0; d += 0.15) {
            // 중심축에 놓일 중앙 좌표
            Location centerPoint = startLoc.clone().add(dir.clone().multiply(d));
            p.getWorld().spawnParticle(Particle.DUST, centerPoint, 1, 0, 0, 0, 0, option);
            centerPoint.getNearbyEntitiesByType(LivingEntity.class, 0.5, 0.5)
                    .stream()
                    .filter(s -> !pm.isParty(p.getUniqueId(), s.getUniqueId()))
                    .forEach(s -> s.damage(2.0, p));

            double radians = d * spiralTightness;
            Vector rotatedOffset = ortho.clone().rotateAroundAxis(dir, radians);

            Location spiralPoint = centerPoint.clone().add(rotatedOffset);

            p.getWorld().spawnParticle(Particle.DUST, spiralPoint, 1, 0, 0, 0, 0, trs);
        }
    }

    @Override
    public void usePassive(Player p) {

    }

    @Override
    public @NotNull Role getRole() {
        return SecondaryRoles.FIGHTER;
    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return null;
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return null;
    }

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return emblem.getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return emblem.getRangeEmblem();
	}
}
