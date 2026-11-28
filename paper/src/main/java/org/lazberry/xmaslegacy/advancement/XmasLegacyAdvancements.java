package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.currency.CurrencyManager;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@Components.Exclude(type = ServerType.LOBBY)
public class XmasLegacyAdvancements implements Initiator {
	public static final String NAMESPACE = "xmaslegacy_global";

	private final Map<UUID, Set<String>> pending = new HashMap<>();
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
		Advancements.setGranter(this::grant);
	}

	@Override
	public void close() {
		Advancements.setGranter(null);
		pending.clear();
	}

	public void setupGlobalAdvancement() {
		globalTab = api.createAdvancementTab(NAMESPACE);
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


		// --- 🎰 [카지노: 주식 투자자에서 오른쪽으로 (Y = 0~2)] ---
		var casinoEnter = builder.register("casino_enter", Casino.entranceTicket(), "한탕의 유혹", AdvancementFrameType.TASK,
				4, 0, buyStock, "카지노에 입장하세요.");

		var coinFlipWin = builder.register("casino_coinflip", Casino.coin(1), "앞이냐 뒤냐", AdvancementFrameType.TASK,
				5, 0, casinoEnter, "코인플립에서 승리하세요.");

		builder.register("casino_coinflip_streak", ItemBuilder.of(plugin, Material.GOLD_NUGGET).setGlint(true).build(), "동전의 신", AdvancementFrameType.CHALLENGE,
				6, 0, coinFlipWin, "코인플립에서 5연승하세요.");

		var slotSpin = builder.register("casino_slot", Material.LEVER, "레버를 당겨라", AdvancementFrameType.TASK,
				5, 1, casinoEnter, "슬롯머신을 돌려보세요.");

		builder.register("casino_jackpot", ItemBuilder.of(plugin, Material.GOLD_BLOCK).setGlint(true).build(), "잭팟", AdvancementFrameType.CHALLENGE,
				6, 1, slotSpin, "슬롯머신에서 잭팟을 터뜨리세요.");

		var versusWin = builder.register("casino_versus", Material.IRON_SWORD, "승부사", AdvancementFrameType.TASK,
				5, 2, casinoEnter, "대결에서 승리하세요.");

		builder.register("casino_versus_streak", Material.NETHERITE_SWORD, "무패의 투사", AdvancementFrameType.GOAL,
				6, 2, versusWin, "대결에서 3연승하세요.");


		// --- ⚒ [직업 숙련도: 백수 탈출에서 오른쪽으로 (Y = 3)] ---
		var roleBronze = builder.register("role_bronze", Material.COPPER_INGOT, "견습 딱지 떼기", AdvancementFrameType.TASK,
				4, 3, useRoleShop, "직업 숙련도 BRONZE를 달성하세요.");

		var roleGold = builder.register("role_gold", Material.GOLD_INGOT, "숙련공", AdvancementFrameType.GOAL,
				5, 3, roleBronze, "직업 숙련도 GOLD를 달성하세요.");

		var roleEmerald = builder.register("role_emerald", Material.EMERALD, "장인의 손길", AdvancementFrameType.GOAL,
				6, 3, roleGold, "직업 숙련도 EMERALD를 달성하세요.");

		var roleImmortal = builder.register("role_immortal", Material.NETHER_STAR, "불멸의 장인", AdvancementFrameType.CHALLENGE,
				7, 3, roleEmerald, "직업 숙련도 IMMORTAL을 달성하세요.");

		builder.register("role_infinite", ItemBuilder.of(plugin, Material.END_CRYSTAL).setGlint(true).build(), "끝없는 길", AdvancementFrameType.CHALLENGE,
				8, 3, roleImmortal, "직업 숙련도 INFINITE를 달성하세요.", "더 이상 오를 곳이 없습니다.");


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


		// --- 📖 [도감: 첫 인사에서 아래로 (Y = 6~10)] ---
		// 4 type completions hang in the x=3 column; the all-complete node sits at (4,6) so its line runs along the empty y=6 row.
		var catalogFirst = builder.register("catalog_first", Material.WRITABLE_BOOK, "첫 기록", AdvancementFrameType.TASK,
				2, 6, talkToNpcBase, "도감에 아이템을 처음으로 등록하세요.");

		builder.register("catalog_farm", Material.WHEAT, "프로 농사꾼", AdvancementFrameType.CHALLENGE,
				3, 7, catalogFirst, "농사 도감을 모두 채우세요.");

		builder.register("catalog_fish", Material.TROPICAL_FISH, "시간은 강태공을 이겨", AdvancementFrameType.CHALLENGE,
				3, 8, catalogFirst, "낚시 도감을 모두 채우세요.");

		builder.register("catalog_mine", Material.DIAMOND, "보석을 주렁주렁", AdvancementFrameType.CHALLENGE,
				3, 9, catalogFirst, "광물 도감을 모두 채우세요.");

		builder.register("catalog_hunt", Material.BONE, "닳아버린 칼날", AdvancementFrameType.CHALLENGE,
				3, 10, catalogFirst, "사냥 도감을 모두 채우세요.");

		builder.register("catalog_all", ItemBuilder.of(plugin, Material.KNOWLEDGE_BOOK).setGlint(true).build(), "엘리안을 위하여", AdvancementFrameType.CHALLENGE,
				4, 6, catalogFirst, "농사·낚시·광물·사냥 도감을", "모두 완성하세요.");

		builder.buildAndRegister(globalTab, root);
		globalTab.automaticallyShowToPlayers().automaticallyGrantRootAdvancement();
	}

	/**
	 * Grants once: does nothing if the player already has it (checked against the advancement plugin's own storage).
	 * Not loaded yet players get it when their data arrives.
	 */
	private void grant(@NotNull Player player, @NotNull String id) {
		if (!Bukkit.isPrimaryThread()) {
			if (plugin.isEnabled()) Bukkit.getScheduler().runTask(plugin, () -> grant(player, id));
			return;
		}
		if (!player.isOnline()) return;

		var advancement = globalTab.getAdvancement(new AdvancementKey(NAMESPACE, id));
		if (advancement == null) {
			log.warn("Unknown advancement id: {}", id);
			return;
		}
		// A failing advancement database must never break the shop, casino etc. that triggered this.
		try {
			if (api.isLoaded(player)) grantGlobalAdvancement(player, advancement);
			else pending.computeIfAbsent(player.getUniqueId(), u -> new LinkedHashSet<>()).add(id);
		} catch (RuntimeException e) {
			log.error("Failed to grant advancement '{}' to {}", id, player.getName(), e);
		}
	}

	/** Player's advancement data arrived: grant what was earned while it was loading. */
	public void grantPending(@NotNull Player player) {
		Set<String> ids = pending.remove(player.getUniqueId());
		if (ids != null) ids.forEach(id -> grant(player, id));
	}

	public void forgetPending(@NotNull UUID uuid) {
		pending.remove(uuid);
	}

	/** @return false if the player already had it; then nothing is announced. */
	public boolean grantGlobalAdvancement(Player player, Advancement advancement) {
		if (advancement.isGranted(player)) return false;
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
		return true;
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
