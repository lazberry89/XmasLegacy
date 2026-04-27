package xmasLegacy.FirstRoleManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Archer extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.SHOCK_DART);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Archer(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@Override
	public void useFirstSkill(Player p) {
        ItemStack bow = p.getInventory().getItemInMainHand();
        if (p.getCooldown(bow) > 0) return;
        if (!consumeEnergy(p, 3)) return;
		p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.6f);

		Arrow arrow = p.launchProjectile(Arrow.class);
		arrow.setVelocity(p.getLocation().getDirection().multiply(2.5));
		arrow.setShooter(p);
		arrow.getPersistentDataContainer().set(new NamespacedKey(getPlugin(), "skill"), PersistentDataType.STRING, "archer_arrow");
		arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isOnGround()) {
                    this.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0.01);
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);

        p.setCooldown(bow, getCooldown1() * 20);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            if (arrow.isValid()) {
                arrow.remove();
            }
        }, 60 * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
        ItemStack tool = p.getInventory().getHelmet();
		if (tool == null || tool.getType() == Material.AIR) return;

        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
            return;
        }
        if (!consumeEnergy(p, 4)) return;
        p.setInvulnerable(true);
        p.getWorld().createExplosion(p.getLocation(), 2, false, false);
        Vector vector = p.getLocation().getDirection();
        p.setVelocity(vector.multiply(-2).setY(0.3));
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            if (p.isValid()) {
                p.setInvulnerable(false);
            }
        }, 10L);
        p.setCooldown(tool, getCooldown2() * 20);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.ARCHER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.BOW)
                .setName(ColorUtils.chat("&8&l궁수의 활"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .hideAllFlags()
                .setTag("role_id", "archer")
                .build()
                .clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.LEATHER_HELMET)
                .setName(ColorUtils.chat("&8&l엘프의 모자"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setUnbreakable()
                .setTag("role_id", "ArcherArmor")
                .hideAllFlags()
                .build()
                .clone();
    }
}
