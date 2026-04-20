package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

public class Priest extends AbstractFirstRole {

	public Priest(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
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
		if (!(entity instanceof Player target)) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 타겟이 아닙니다!"));
			return;
		}
		target.removePotionEffect(PotionEffectType.REGENERATION);

		target.addPotionEffect(new org.bukkit.potion.PotionEffect(
				org.bukkit.potion.PotionEffectType.REGENERATION,
				duration,
				amplifier
		));
		target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
		target.getWorld().playSound(p.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.0f);
		p.setCooldown(tool, this.getCooldown1() * 20);
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


	}

	@Override
	public Roles getRole() {
		return Roles.PRIEST;
	}

	@Override
	public ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_SPEAR)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.addAttribute(Attribute.ATTACK_DAMAGE, 5.0, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.GOLDEN_CHESTPLATE)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(5.0, null)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, 5.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.build()
				.clone();
	}
}
