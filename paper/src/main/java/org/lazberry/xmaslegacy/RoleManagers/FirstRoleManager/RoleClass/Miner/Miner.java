package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Miner;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.XmasLegacy;

@Roles
public class Miner extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	private final @NotNull ChainMining chain = new ChainMining();
	private final @NotNull OreEye eye = new OreEye();

	public record Container(
		XmasLegacy plugin,
		int first_skill_hunger_cost,
		int first_skill_target_range,
		int first_skill_ore_chain_loop,
		int second_skill_hunger_cost,
		int second_skill_scan_range,
		long second_skill_glow_duration
	) implements RoleContainer {}

	public Miner() {
		super(BasicRoles.MINER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_target_range", 7)
				.setDefault("stats.first_skill_ore_chain_loop", 5)
				.setDefault("stats.second_skill_hunger_cost", 3)
				.setDefault("stats.second_skill_scan_range", 15)
				.setDefault("stats.second_skill_glow_duration", 40L)
				.setDefault("tool.role_weapon", "IRON_PICKAXE")
				.setDefault("tool.role_armor", "IRON_CHESTPLATE");
		this.container = new Container(
				getPlugin(),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_target_range", 7),
				configs.getValue("stats.first_skill_ore_chain_loop", 5),
				configs.getValue("stats.second_skill_hunger_cost", 3),
				configs.getValue("stats.second_skill_scan_range", 15),
				configs.getValue("stats.second_skill_glow_duration", 40L)
		);

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.IRON_PICKAXE);
		this.armor_item = ParseItem.parse(config.getString("tool.role_armor"), Material.IRON_CHESTPLATE);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, chain, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, eye, container, getCooldown2());
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&l광부의 곡괭이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l철제 보호구"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setRoleDefault(this.getRole())
				.hideAllFlags()
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
      		&0&l[ &8&l광부 가이드 &0&l ]&r
      	
      		&0광부는 대지의 깊은 곳에서
      		&0희귀한 자원을 찾아내 서버의
      		&0&l경제&r&0를 지탱하는 전문가입니다.
      
      		&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &8&o준비 중
      		&0- &83차 전직: &8&o준비 중
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&8&l▶ &0&l연쇄 광질 &8[%d초]
      		&0지맥의 흐름을 읽어 범위 내의
      		&0모든 광석을 &b&l한번에&r&0 채굴합니다.
      
      		&8&l▶ &0&l광부의 눈 &8[%d초]
      		&0숨겨진 광물을 감지하여 짧은
      		&0시간 동안 위치를 &e&l발광&r&0시킵니다.
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("광부", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}