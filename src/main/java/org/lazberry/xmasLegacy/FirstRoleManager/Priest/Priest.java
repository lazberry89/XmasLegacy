package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Skill.BasicSkills;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.User.PartyManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.GlowUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Priest extends AbstractFirstRole {
    private final PartyManager PM;
    private final SkillEffectManager SEM;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.COMPACT_HEAL);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Priest(int c1, int c2, PartyManager PM, SkillEffectManager SEM, XmasLegacy plugin) {
		super(c1, c2, plugin);
        this.PM = PM;
        this.SEM = SEM;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, 3)) return;
		int duration = 5 * 20;
		int amplifier = 1;
		Entity entity = p.getTargetEntity(15, false);
        if (entity != null) {
            if (!(entity instanceof Player target) || !PM.isParty(p, target)) {
                p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 타겟이 아닙니다!"));
                GlowUtils.setGlowColor(entity, NamedTextColor.RED);
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    if (entity.isValid()) {
                        GlowUtils.clearGlow(entity);
                    }
                }, 10L);
                return;
            }
            target.removePotionEffect(PotionEffectType.REGENERATION);
            GlowUtils.setGlowColor(target, NamedTextColor.GREEN);
            target.addPotionEffect(new PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION,
                    duration,
                    amplifier
            ));
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
            target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
            target.getWorld().playSound(p.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.0f);
            p.setCooldown(tool, this.getCooldown1() * 20);
        }
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
        int duration = 5 * 20;
        int amplifier = 1;

        Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.0f);
        SEM.drawCircularLine(p.getLocation().add(0, 0.2, 0),
                Particle.DUST, 7, false, 100, dust);
        for (Entity ally : p.getNearbyEntities(5, 5, 5)) {
            if (!(ally instanceof Player target)) continue;
            if (PM.isParty(p, target)) {
                target.removePotionEffect(PotionEffectType.STRENGTH);
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH,
                        duration,
                        amplifier
                ));
                GlowUtils.setGlowColor(target, NamedTextColor.YELLOW);
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
            }
        }
        p.setCooldown(tool, this.getCooldown2() * 20);

	}

	@Override
	public Roles getRole() {
		return Roles.PRIEST;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_SPEAR)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "priest")
				.addAttribute(Attribute.ATTACK_DAMAGE, 5.0, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_CHESTPLATE)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(5.0, EquipmentSlotGroup.CHEST)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, 5.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setTag("role_id", "priest")
				.build()
				.clone();
	}
}
