package xmasLegacy.FirstRoleManager;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration; // 💡 [추가] 설정 파일 연동을 위한 임포트
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class Crafter extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.FIX);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

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
	private static Crafter instance;

	public static Crafter getInstance() {
		if (instance == null) instance = new Crafter();
		return instance;
	}

	private Crafter() {
		super(Roles.CRAFTER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

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
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
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

		// 1. 내구도가 있는 아이템인지 확인 (검, 곡괭이 등)
		if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 수리할 수 없는 아이템입니다!"));
			return;
		}

		// 2. 현재 대미지 확인 (0이면 새것)
		int currentDamage = damageable.getDamage();
		if (currentDamage <= 0) {
			p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 이미 새 아이템입니다!"));
			return;
		}

		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;

		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		double percent = this.first_skill_repair_percent;
		int repairAmount = (int) (itemStack.getType().getMaxDurability() * percent);
		int newDamage = Math.max(0, currentDamage - repairAmount);

		damageable.setDamage(newDamage);
		itemStack.setItemMeta(damageable);
		itemEntity.setItemStack(itemStack);

		// 5. 피드백
		p.sendMessage(ColorUtils.chat(Alert.GREEN + " 성공적으로 수리했습니다! &7(수리량: " + repairAmount + ")"));
		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		p.setCooldown(p.getInventory().getItemInMainHand().getType(), this.first_skill_cooldown_ticks);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null) return;

		// 1. 쿨타임 체크
		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}

		// 2. 타겟 아이템 엔티티 확인
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		Entity target = p.getTargetEntity(this.second_skill_raytrace_range, false);
		if (!(target instanceof Item itemEntity)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 아이템(드롭된 아이템)을 조준해주세요!"));
			return;
		}

		ItemStack itemStack = itemEntity.getItemStack();
		ItemMeta meta = itemStack.getItemMeta();
		if (meta == null) return;

		// 3. 중복 강화 확인 (PDC 사용)
		org.bukkit.NamespacedKey buffKey = new org.bukkit.NamespacedKey(getPlugin(), "crafter_buff");
		if (meta.getPersistentDataContainer().has(buffKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 장인의 가호가 깃든 아이템입니다!"));
			return;
		}

		// 4. 아이템 종류 판별 및 속성 부여
		String materialName = itemStack.getType().name();
		boolean isApplied = false;

		if (materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE") || materialName.endsWith("_SHOVEL") || materialName.endsWith("_HOE")) {
			// [도구] 채굴 속도 상승 (효율 1단계 정도의 수치 추가)
			// 💡 하드코딩 제거 및 설정 파일 변수 적용
			org.bukkit.attribute.AttributeModifier speedMod = new org.bukkit.attribute.AttributeModifier(
					buffKey, this.second_skill_mining_efficiency_buff, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(org.bukkit.attribute.Attribute.MINING_EFFICIENCY, speedMod);

			updateLore(meta, "&6[장인의 가호: 채굴 속도 증가]");
			isApplied = true;

		} else if (materialName.endsWith("_SWORD") || materialName.equals("TRIDENT")) {
			// [무기] 공격력 상승 (공격력 +2)
			// 💡 하드코딩 제거 및 설정 파일 변수 적용
			org.bukkit.attribute.AttributeModifier damageMod = new org.bukkit.attribute.AttributeModifier(
					buffKey, this.second_skill_attack_damage_buff, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageMod);

			updateLore(meta, "&c[장인의 가호: 공격력 증가]");
			isApplied = true;
		}

		if (!isApplied) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 강화할 수 있는 장비(무기/도구)가 아닙니다!"));
			return;
		}

		// 5. 에너지 소모 및 최종 적용
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;

		// 중복 방지 태그 저장
		meta.getPersistentDataContainer().set(buffKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
		itemStack.setItemMeta(meta);
		itemEntity.setItemStack(itemStack);

		// 6. 피드백
		p.sendMessage(ColorUtils.chat(Alert.GREEN + " 장비에 임시 강화를 부여했습니다!"));
		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		p.setCooldown(tool.getType(), this.second_skill_cooldown_ticks);
	}

	// 로어에 강화 정보를 추가해주는 헬퍼 메서드
	private void updateLore(@NotNull ItemMeta meta, String text) {
		List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
		if (lore == null) return;
		lore.add(ColorUtils.chat(""));
		lore.add(ColorUtils.chat(text));
		meta.lore(lore);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.CRAFTER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l장인의 손길"))
				.setLore(ColorUtils.chat("&7장인의 땀과 기술이 담긴 장비입니다."))
				.hideAllFlags()
				.setTag("role_id", "crafter")
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l위험한 작업으로부터 장인을 보호해줍니다."))
				.setLore(ColorUtils.chat("&7장인의 경험과 기술이 담긴 갑옷입니다."))
				.hideAllFlags()
				.setTag("role_id", "crafter")
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 그림자 속의 암살자 설명
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