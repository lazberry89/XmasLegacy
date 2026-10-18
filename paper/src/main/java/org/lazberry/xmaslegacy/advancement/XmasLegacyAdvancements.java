package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.currency.CurrencyManager;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

@Getter
@Registry.Exclude(type = ServerType.LOBBY)
public class XmasLegacyAdvancements implements Initiator {
	private final XmasLegacy plugin;
	private final UltimateAdvancementAPI api;
	private final AdvancementBuilder builder;

	private AdvancementTab globalTab;
	private AdvancementTab wildTab;

	@Inject
	public XmasLegacyAdvancements(XmasLegacy plugin, AdvancementBuilder builder) {
		this.plugin = plugin;
		this.api = UltimateAdvancementAPI.getInstance(plugin);
		this.builder = builder;
	}

	@Override
	public void init() {
		setupGlobalAdvancement();
	}

	public void setupGlobalAdvancement() {
		globalTab = api.createAdvancementTab("xmaslegacy_global");
		AdvancementDisplay rootDisplay = new AdvancementDisplay(
				Material.ICE,
				"얼어붙은 세계",
				AdvancementFrameType.CHALLENGE,
				true, false,
				0, 3,
				"얼어붙은 세계에 도전하세요."
		);
		RootAdvancement root = new RootAdvancement(
				globalTab,
				"world_start",
				rootDisplay,
				"textures/block/packed_ice.png"
		);

		// 2. NPC 첫 인사 (중앙 분기점: X = 1, Y = 3)
		var talkToNpcBase = builder.register("talk_npc", Material.EMERALD, "첫 인사", AdvancementFrameType.TASK,
				1, 3, root, "마을 주민들에게 인사를 건내보세요.");


		// --- 💰 [위쪽 브랜치: 경제 시스템] ---
		var earnMoney = builder.register("earn_money", CurrencyManager.currency(plugin), "피, 땀, 눈물", AdvancementFrameType.GOAL,
				2, 1, talkToNpcBase, "돈을 벌고 이 세계의 구성원이 되세요.");

		var buyStock = builder.register("buy_stock", ItemBuilder.of(plugin, Material.FLOW_BANNER_PATTERN).setGlint(true).build(), "주식 투자자", AdvancementFrameType.TASK,
				3, 0, earnMoney, "주식에 투자하세요.");

		var buyRegion = builder.register("buy_region", RegionManager.RegionTicket(), "나만의 땅", AdvancementFrameType.TASK,
				3, 1, earnMoney, "부동산에서 땅 점유권을 구매하세요.");

		var useDeathInsurance = builder.register("death_insurance", Material.CHEST, "보험처리", AdvancementFrameType.TASK,
				3, 2, earnMoney, "사망처리 보험을 사용하세요.");

		var useRoleShop = builder.register("use_role_shop", Material.LECTERN, "백수 탈출", AdvancementFrameType.GOAL,
				3, 3, earnMoney, "직업을 얻고 직업상점을 이용하세요.");


		// --- 🌾 [중간 브랜치: 생존/농사 (Y = 3)] ---
		var torchFlowerPlantBase = builder.register("plant_torch_flower", Material.TORCHFLOWER_SEEDS, "온기를 심다", AdvancementFrameType.TASK,
				2, 4, talkToNpcBase, "한기를 유지하기 위해", "작물을 재배하세요.");

		var sunFlowerBreadCreateBase = builder.register("sunflower_bread_create", Material.TORCHFLOWER, "온기를 담아", AdvancementFrameType.GOAL,
				3, 4, torchFlowerPlantBase, "온기를 담은 음식을 제작하세요.");

		builder.register("consume_sunflower_bread", AgeableCrops.SunFlowerBread(), "한기에 대적", AdvancementFrameType.CHALLENGE,
				4, 4, sunFlowerBreadCreateBase, "얼어붙은 세계의 한기에 대적하여", "몸을 따듯하게 유지하세요.");


		// --- 🔨 [아래쪽 브랜치: 강화/인챈트 (Y = 5)] ---
		var enchantBase = builder.register("first_enchant", EnchantMaterial.PrismFractal(), "추운 곳에 낙원은 없다", AdvancementFrameType.TASK,
				2, 5, talkToNpcBase, "살아남기 위해 강해지세요.");

		var downgradeProtectBase = builder.register("downgrade_protect", EnchantMaterial.DowngradeProtector(), "녹지 않는 얼음", AdvancementFrameType.TASK,
				3, 5, enchantBase, "마력이 깃든 아이템을 사용하여 장비를 보호하세요.");

		var breakProtectBase = builder.register("break_protect", EnchantMaterial.BreakProtector(), "꺼지지않는 불꽃", AdvancementFrameType.GOAL,
				4, 5, downgradeProtectBase, "실패를 두려워하지 마세요.");

		var boosterUse = builder.register("booster_use", EnchantMaterial.BoosterFivePercent(), "피로 물든 종이", AdvancementFrameType.TASK,
				5, 5, breakProtectBase, "강화서의 도움을 받아 강해지세요.");

		builder.register("booster_all_use", EnchantMaterial.BoosterTenPercent(), "혼자가 아니야", AdvancementFrameType.CHALLENGE,
				6, 5, boosterUse, "모든 강화서의 도움을 받으세요.");

		builder.buildAndRegister(globalTab, root);
		globalTab.automaticallyShowToPlayers();
	}

	public void grantGlobalAdvancement(Player player, String id) {
		var key = new AdvancementKey(plugin, id);
		var advancement = api.getAdvancement(key);

		grantGlobalAdvancement(player, advancement);
	}

	public void grantGlobalAdvancement(Player player, Advancement advancement) {
		if (advancement != null) {
			advancement.grant(player);

			var display = advancement.getDisplay();
			String title = display.getTitle();
			String name = player.getName();
			AdvancementFrameType type = display.getFrame();

			Component prefix = ColorUtils.chat("&f" + name + "이(가) ");
			Component advance = titleMaker(type, title)
					.hoverEvent(HoverEvent.showText(ColorUtils.chat(display.getCompactDescription())));
			Component end = ColorUtils.chat(" &f도전을 완료했습니다.");

			Bukkit.broadcast(prefix.append(advance).append(end));
			player.playSound(player, getSoundByAdvancementType(type), 1.0f, 1.0f);
		}
	}

	private Component titleMaker(AdvancementFrameType type, String title) {
		if (type == AdvancementFrameType.CHALLENGE)
			return ColorUtils.minimessage("<gradient:#45F7FF:#762BDE>[" + title + "]</gradient>");
		else return ColorUtils.chat("&b[" + title + "]");
	}

	private Sound getSoundByAdvancementType(AdvancementFrameType type) {
		return switch (type) {
			case CHALLENGE -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
			case GOAL -> Sound.ENTITY_PLAYER_LEVELUP;
			case TASK -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
		};
	}


}
