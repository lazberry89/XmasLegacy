package xmasLegacy.FirstRoleManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rogue extends AbstractFirstRole {
	private final SkillEffectManager SEM;
    private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
    public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.DAGGER_RUSH);}
    public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Rogue(int c1, int c2, SkillEffectManager SEM, XmasLegacy plugin) {
		super(c1, c2, plugin);
		this.SEM = SEM;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getBoots();
		if (tool == null || tool.getType() == Material.AIR) return;
		Entity target = p.getTargetEntity(10, false);

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (target != null) {
			if (target instanceof LivingEntity le) {
				if (!consumeEnergy(p, 3)) return;

                Vector vector = p.getLocation().getDirection().normalize();
                p.setVelocity(vector.multiply(2.5).setY(0.2)); // 너무 빠르면 감지가 안 될 수 있어 2.5 추천
                SEM.followParticle(p, Particle.DUST, 0.5, new Particle.DustOptions(Color.GRAY, 1.5f));

                p.setCooldown(tool, this.getCooldown1() * 20);

                new BukkitRunnable() {
                    int timeout = 0;

                    @Override
                    public void run() {
                        timeout++;

                        if (p.getLocation().distance(le.getLocation()) <= 2.0) {
                            useDaggerRush(p, le);
                            this.cancel();
                            return;
                        }

                        if (timeout > 20 || !p.isOnline() || le.isDead()) {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(getPlugin(), 0L, 1L);

                p.setCooldown(tool, this.getCooldown1() * 20);
			} else {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 타겟이 아닙니다!"));
			}
		} else {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 타겟이 없습니다!"));
		}
	}

	private void useDaggerRush(Player player, LivingEntity target) {
		new BukkitRunnable() {
			int count = 0;

			@Override
			public void run() {
				if (count >= 5 || !target.isValid() || target.isDead()) {
					this.cancel();
					return;
				}
				target.setNoDamageTicks(0);

				target.damage(2, player);

				target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

				count++;
			}
		}.runTaskTimer(getPlugin(), 0L, 2L);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemStack[] armorContents = p.getInventory().getArmorContents().clone();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
        if (!consumeEnergy(p, 3)) return;
		Particle.DustOptions dust = new Particle.DustOptions(Color.GRAY, 5.0f);
		p.getWorld().spawnParticle(Particle.DUST, p.getLocation(), 160, 5, 3, 5, 0.01, dust); //연막 컨셉
		p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.8f);
		p.setInvisible(true);


		p.getInventory().setArmorContents(new ItemStack[4]);

		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (p.isValid()) {
				p.setInvisible(false);
				if (armorContents != null) {
					p.getInventory().setArmorContents(armorContents);
				}
			}
		}, 100L);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.ROGUE;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_SWORD)
				.setName(ColorUtils.chat("&7&l무딘 단검"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				//.setItemModel("knife")
				.setTag("role_id", "rogue")
				.setAttackDamage(3.0)
				.addAttribute(Attribute.MOVEMENT_SPEED, 0.02, AttributeModifier.Operation.ADD_NUMBER)
				.addAttribute(Attribute.ATTACK_SPEED, 0.02, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_BOOTS)
		        .setName(ColorUtils.chat("&7&l낡은 부츠"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setUnbreakable()
		        .hideAllFlags()
		        .setItemModel("RogueArmor")
                .setTag("role_id", "RogueArmor")
		        .setArmorState(5.0, EquipmentSlotGroup.FEET)
		        .build()
		        .clone();
    }

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 그림자 속의 암살자 설명
		String page1 = """
          &8&l[ THIEF ]
          
          &7도적은 &8&l'그림자'&r&7 속에 숨어
          민첩한 기동성과 숙련도에 따라
          치명적인 수를 두는 암살자입니다.
          
          &8&m-----------------
          &8&l[ ADVANCE ]
          &8- &72차: 어쌔신, 리퍼
          &8- &73차: &8&l[Mr.Shadow]
          """;

		// [페이지 2] 변칙적이고 화려한 스킬 설명
		String page2 = String.format("""
          &8&l[ &a&lSKILLS ]
          
          &8&l▶ 돌진기 [%d초]
          &7표적을 포착해 연속으로
          &7급소를 빠르게 찔러 가릅니다.
          
          &8&l▶ &e&l연막탄 [%d초]
          &7자취를 감추고 신속의 힘으로
          &7전장을 유유히 빠져나갑니다.
          
          &8&m-----------------
          """, getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("도적", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
