package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Knight extends AbstractFirstRole {
    private float Damage = 5;
    private final SkillEffectManager SEM;

	public Knight(int c1, int c2, SkillEffectManager SEM) {
		super(c1, c2);
        this.SEM = SEM;
	}

	@Override
	public void useFirstSkill(Player player) { //Sharp Sweeping
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (player.getCooldown(tool) > 0) {
            player.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + player.getCooldown(tool) * 20 + "&f초 기다리세요"));
            return;
        }

        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(1.5).setY(0.2));

        player.setPose(Pose.SPIN_ATTACK);

        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 1);
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 10; // 0.5초 동안 지속 (20틱 = 1초)
            final Set<UUID> hitEntities = new HashSet<>();

            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    player.setPose(Pose.STANDING);
                    this.cancel();
                    return;
                }
                player.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 1);
                player.setPose(Pose.SPIN_ATTACK);
                Vector currentV = player.getVelocity();
                player.setVelocity(currentV.add(new Vector(0, 0.05, 0)));

                for (Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity target && !entity.equals(player)) {

                        if (hitEntities.contains(target.getUniqueId())) continue;

                        target.damage(5.0, player); // 5 대미지 (하트 2.5칸)
                        target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2, false, false, true));

                        Vector push = direction.clone().multiply(0.5).setY(0.2);
                        target.setVelocity(push);

                        hitEntities.add(target.getUniqueId());
                    }
                }

                ticks++;
            }
        }.runTaskTimer(getPlugin(), 0L, 1L); // 매 틱(1/20초)마다 실행
        player.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) { //Heavy Strike
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (p.getCooldown(tool) > 0) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + p.getCooldown(tool) * 20 + "&f초 기다리세요"));
            return;
        }
        SEM.drawCircularLine(p, p.getLocation().add(0, 0.1, 0), Particle.CRIT, 2.5, true, 50);
        for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), 2.5, 3, 2.5)) {
            if (entity instanceof LivingEntity e) {
                if (p.equals(e)) continue;
                SEM.knockbackEntity(p, e, 1.5, 0.15);
                e.damage(Damage/2);
                p.playSound(p.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.0f, 1.0f);
            }
        }
        p.setCooldown(tool, this.getCooldown2() * 20);
	}
	@Override
	public Roles getRole() {
		return Roles.Knight;
	}

	@Override
	public ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_SWORD)
                .setName(ColorUtils.chat("&7&l녹슨 철검"))
                .setLore(ColorUtils.chat("&5기사의 검.장인은 도구를 탓하지 않는다."), ColorUtils.chat("&e&l공격력:&7&l " + this.Damage))
                .setUnbreakable()
                .hideAllFlags()
                .setItemModel("BasicSword")
                .setAttackDamage(this.Damage)
                .setTag("role_id", "knight")
                .build()
                .clone();
	}

    @Override
    public ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
                .setName(ColorUtils.chat("&7&l낡은 흉갑"))
                .setLore("&5장인은 도구를 탓하지 않는다.근데 이건 좀 불편함")
                .setUnbreakable()
                .hideAllFlags()
                .setItemModel("KnightArmor")
                .build()
                .clone();
    }
}
