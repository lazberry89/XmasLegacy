package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Crafter;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.ArrayList;
import java.util.List;

@Skill(type = PlayerSkills.TEMP_BUFF)
public class TempBuff implements Skills<Crafter.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Crafter.@NotNull Container container) {
		Entity target = caster.getTargetEntity(container.second_skill_raytrace_range(), false);
		if (!(target instanceof Item itemEntity)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 아이템(드롭된 아이템)을 조준해주세요!"));
			return false;
		}

		ItemStack itemStack = itemEntity.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		if (meta == null) return false;


		NamespacedKey buffKey = KeyUtils.get("crafter_buff");
		if (meta.getPersistentDataContainer().has(buffKey, PersistentDataType.BYTE)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 이미 장인의 가호가 깃든 아이템입니다!"));
			return false;
		}

		String materialName = itemStack.getType().name();
		boolean isApplied = false;

		if (materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE") || materialName.endsWith("_SHOVEL") || materialName.endsWith("_HOE")) {
			AttributeModifier speedMod = new AttributeModifier(
					buffKey, container.second_skill_mining_efficiency_buff(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.MINING_EFFICIENCY, speedMod);

			updateLore(meta, "&6[장인의 가호: 채굴 속도 증가]");
			isApplied = true;

		} else if (materialName.endsWith("_SWORD") || materialName.equals("TRIDENT")) {
			AttributeModifier damageMod = new AttributeModifier(
					buffKey, container.second_skill_attack_damage_buff(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageMod);

			updateLore(meta, "&c[장인의 가호: 공격력 증가]");
			isApplied = true;
		}

		if (!isApplied) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 수 있는 장비(무기/도구)가 아닙니다!"));
			return false;
		}
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;

		meta.getPersistentDataContainer().set(buffKey, PersistentDataType.BYTE, (byte) 1);
		itemStack.setItemMeta(meta);
		itemEntity.setItemStack(itemStack);

		caster.sendMessage(ColorUtils.chat(Alert.GREEN + " 장비에 임시 강화를 부여했습니다!"));
		caster.playSound(caster.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
		return true;
	}

	private void updateLore(@NotNull ItemMeta meta, String text) {
		List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
		if (lore == null) return;
		lore.add(ColorUtils.chat(""));
		lore.add(ColorUtils.chat(text));
		meta.lore(lore);
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.TEMP_BUFF;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.CRAFTER;
	}
}
