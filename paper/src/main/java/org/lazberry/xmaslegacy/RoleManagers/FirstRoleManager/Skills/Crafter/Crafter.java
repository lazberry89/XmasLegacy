package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Crafter;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.settings.Alert;

import java.util.ArrayList;
import java.util.List;

@Roles
public class Crafter extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private int first_skill_raytrace_range;
	private int first_skill_hunger_cost;
	private double first_skill_repair_percent;
	private int first_skill_cooldown_ticks;
	private int second_skill_raytrace_range;
	private double second_skill_mining_efficiency_buff;
	private double second_skill_attack_damage_buff;
	private int second_skill_hunger_cost;
	private int second_skill_cooldown_ticks;

	private Container container;

	private final @NotNull Skills<Crafter.Container> fix = new Fix();
	private final @NotNull Skills<Crafter.Container> tempBuff = new TempBuff();

	public Crafter() {
		super(BasicRoles.CRAFTER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
		ItemStack item,
		Item item_entity,
		Damageable damageable,
		int current_damage,
		int first_skill_raytrace_range,
		int first_skill_hunger_cost,
		double first_skill_repair_percent,
		int first_skill_cooldown_ticks,
		int second_skill_raytrace_range,
		double second_skill_mining_efficiency_buff,
		double second_skill_attack_damage_buff,
		int second_skill_hunger_cost,
		int second_skill_cooldown_ticks
	) implements RoleContainer {}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		// 1. 장인 전용 YAML 스탯 기본값 주입
		config.addDefault("stats.first_skill_raytrace_range", 5);
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_repair_percent", 0.21);
		config.addDefault("stats.first_skill_cooldown_ticks", 100);

		config.addDefault("stats.second_skill_raytrace_range", 5);
		config.addDefault("stats.second_skill_mining_efficiency_buff", 2.0);
		config.addDefault("stats.second_skill_attack_damage_buff", 2.0);
		config.addDefault("stats.second_skill_hunger_cost", 5);
		config.addDefault("stats.second_skill_cooldown_ticks", 200);

		config.addDefault("tool.role_weapon", "ANVIL");
		config.addDefault("tool.role_armor", "IRON_CHESTPLATE");

		// 2. 파일 변수 바인딩 수립
		this.first_skill_raytrace_range = config.getInt("stats.first_skill_raytrace_range", 5);
		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_repair_percent = config.getDouble("stats.first_skill_repair_percent", 0.21);
		this.first_skill_cooldown_ticks = config.getInt("stats.first_skill_cooldown_ticks", 100);

		this.second_skill_raytrace_range = config.getInt("stats.second_skill_raytrace_range", 5);
		this.second_skill_mining_efficiency_buff = config.getDouble("stats.second_skill_mining_efficiency_buff", 2.0);
		this.second_skill_attack_damage_buff = config.getDouble("stats.second_skill_attack_damage_buff", 2.0);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 5);
		this.second_skill_cooldown_ticks = config.getInt("stats.second_skill_cooldown_ticks", 200);

		// 3. 재질 에러 검증 및 캐싱
		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.ANVIL;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.IRON_CHESTPLATE;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();

		Entity target = p.getTargetEntity(this.first_skill_raytrace_range, false);
		if (target == null) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 대상이 없습니다!"));
			return;
		}
		if (!(target instanceof Item itemEntity)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 아이템(드롭된 아이템)을 조준해주세요!"));
			return;
		}
		ItemStack itemStack = itemEntity.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();

		if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 수 없는 아이템입니다!"));
			return;
		}

		int currentDamage = damageable.getDamage();
		if (currentDamage <= 0) {
			p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 이미 새 아이템입니다!"));
			return;
		}

		this.container = new Container(
				itemStack,
				itemEntity,
				damageable,
				currentDamage,
				first_skill_raytrace_range,
				first_skill_hunger_cost,
				first_skill_repair_percent,
				first_skill_cooldown_ticks,
				second_skill_raytrace_range,
				second_skill_mining_efficiency_buff,
				second_skill_attack_damage_buff,
				second_skill_hunger_cost,
				second_skill_cooldown_ticks
		);

		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		fix.execute(p, container);
		p.setCooldown(tool, this.first_skill_cooldown_ticks);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();

		Entity target = p.getTargetEntity(this.second_skill_raytrace_range, false);
		if (!(target instanceof Item itemEntity)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 아이템(드롭된 아이템)을 조준해주세요!"));
			return;
		}

		ItemStack itemStack = itemEntity.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		if (meta == null) return;

		// 3. 중복 강화 확인 (PDC 사용)
		NamespacedKey buffKey = KeyUtils.get("crafter_buff");
		if (meta.getPersistentDataContainer().has(buffKey, PersistentDataType.BYTE)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 장인의 가호가 깃든 아이템입니다!"));
			return;
		}

		this.container = new Container(
				itemStack,
				itemEntity,
				null,
				0,
				first_skill_raytrace_range,
				first_skill_hunger_cost,
				first_skill_repair_percent,
				first_skill_cooldown_ticks,
				second_skill_raytrace_range,
				second_skill_mining_efficiency_buff,
				second_skill_attack_damage_buff,
				second_skill_hunger_cost,
				second_skill_cooldown_ticks
		);

		// 4. 아이템 종류 판별 및 속성 부여
		String materialName = itemStack.getType().name();
		boolean isApplied = false;

		if (materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE") || materialName.endsWith("_SHOVEL") || materialName.endsWith("_HOE")) {
			AttributeModifier speedMod = new AttributeModifier(
					buffKey, this.second_skill_mining_efficiency_buff, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.MINING_EFFICIENCY, speedMod);

			updateLore(meta, "&6[장인의 가호: 채굴 속도 증가]");
			isApplied = true;

		} else if (materialName.endsWith("_SWORD") || materialName.equals("TRIDENT")) {
			AttributeModifier damageMod = new AttributeModifier(
					buffKey, this.second_skill_attack_damage_buff, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageMod);

			updateLore(meta, "&c[장인의 가호: 공격력 증가]");
			isApplied = true;
		}

		if (!isApplied) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 수 있는 장비(무기/도구)가 아닙니다!"));
			return;
		}
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;

		meta.getPersistentDataContainer().set(buffKey, PersistentDataType.BYTE, (byte) 1);
		itemStack.setItemMeta(meta);
		itemEntity.setItemStack(itemStack);

		p.sendMessage(ColorUtils.chat(Alert.GREEN + " 장비에 임시 강화를 부여했습니다!"));
		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
		p.setCooldown(tool.getType(), this.second_skill_cooldown_ticks);
	}

	private void updateLore(@NotNull ItemMeta meta, String text) {
		List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
		if (lore == null) return;
		lore.add(ColorUtils.chat(""));
		lore.add(ColorUtils.chat(text));
		meta.lore(lore);
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l장인의 손길"))
				.setLore(ColorUtils.chat("&7장인의 땀과 기술이 담긴 장비입니다."))
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l위험한 작업으로부터 장인을 보호해줍니다."))
				.setLore(ColorUtils.chat("&7장인의 경험과 기술이 담긴 갑옷입니다."))
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return getEmblem().getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return getEmblem().getRangeEmblem();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
          &0&l[ &8&l장인 가이드 &0&l ]&r
          
          &0도적은 그림자 속에 숨어들어
          &0적의 빈틈을 노리는 &b&l민첩함&r&0과
          &0기술이 핵심인 암살자입니다.
          
          &7&m-----------------
          &0&l[ &1&l전직 계보 &0&l ]&r
          &0- &82차 전직: &0대장장이, 연금술사
          &0- &83차 전직: &8&o준비 중
          """;

		String page2 = String.format("""
          &0&l[ &2&l보유 스킬 &0&l ]&r

          &8&l▶ &0&l수리하기 &8[%d초]
          &0고객의 장비, 무기 등의 손상된 내구도를
          &0일정량 복구해준다.
          
          &8&l▶ &0&l일시버프 &8[%d초]
          &0도구, 무기에 맞게 한번에 한하여
          &0일정 버프가 붙는다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("장인", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}