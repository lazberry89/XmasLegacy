package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
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
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;

@Getter
@Registry.Exclude(type = ServerType.LOBBY)
public class XmasLegacyAdvancements implements Initiator {
	private final XmasLegacy plugin;
	private final UltimateAdvancementAPI api;
	private AdvancementTab globalTab;
	private AdvancementTab wildTab;

	@Inject
	public XmasLegacyAdvancements(XmasLegacy plugin) {
		this.plugin = plugin;
		this.api = UltimateAdvancementAPI.getInstance(plugin);
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
				0, 1,
				"얼어붙은 세계에 도전하세요."
		);
		RootAdvancement root = new RootAdvancement(
				globalTab,
				"world_start",
				rootDisplay,
				"textures/block/packed_ice.png"
		);
		//
		AdvancementDisplay torchFlowerPlantDisplay = new AdvancementDisplay(
				Material.TORCHFLOWER_SEEDS,
				"온기를 심다",
				AdvancementFrameType.CHALLENGE,
				true, false,
				1, 1,
				"한기를 유지하기 위해", "작물을 재배하세요."
		);
		BaseAdvancement torchFlowerPlantBase = new BaseAdvancement(
				"plant_torch_flower",
				torchFlowerPlantDisplay,
				root
		);
		//
		AdvancementDisplay sunflowerBreadCreate = new AdvancementDisplay(
				Material.TORCHFLOWER,
				"온기를 담아",
				AdvancementFrameType.CHALLENGE,
				true, false,
				2, 1,
				"온기를 담은 음식을 제작하세요."
		);
		BaseAdvancement sunFlowerBreadCreateBase = new BaseAdvancement(
				"sunflower_bread_create",
				sunflowerBreadCreate,
				torchFlowerPlantBase
		);
		//
		AdvancementDisplay consumeBreadDisplay = new AdvancementDisplay(
				AgeableCrops.SunFlowerBread(),
				"한기에 대적",
				AdvancementFrameType.CHALLENGE,
				true, false,
				3, 1,
				"얼어붙은 세계의 한기에 대적하여", "몸을 따듯하게 유지하세요."
		);
		BaseAdvancement consumeBreadBase = new BaseAdvancement(
				"consume_sunflower_bread",
				consumeBreadDisplay,
				sunFlowerBreadCreateBase
		);
		//
 		// enchant
		AdvancementDisplay enchant = new AdvancementDisplay(
				EnchantMaterial.PrismFractal(),
				"추운 곳에 낙원은 없다",
				AdvancementFrameType.CHALLENGE,
				true, false,
				1, 3,
				"살아남기 위해 강해지세요."
		);
		BaseAdvancement enchantBase = new BaseAdvancement(
			"first_enchant",
			enchant,
			root
		);
		//
		AdvancementDisplay downgradeProtectDisplay = new AdvancementDisplay(
				EnchantMaterial.DowngradeProtector(),
				"녹지 않는 얼음",
				AdvancementFrameType.CHALLENGE,
				true, false,
				2, 3,
				"마력이 깃든 아이템을 사용하여 장비를 보호하세요."
		);
		BaseAdvancement downgradeProtectBase = new BaseAdvancement(
				"downgrade_protect",
				downgradeProtectDisplay,
				enchantBase
		);
		//
		AdvancementDisplay breakProtectDisplay = new AdvancementDisplay(
				EnchantMaterial.BreakProtector(),
				"꺼지지않는 불꽃",
				AdvancementFrameType.CHALLENGE,
				true, false,
				3, 3,
				"실패를 두려워하지 마세요."
		);
		BaseAdvancement breakProtectBase = new BaseAdvancement(
				"break_protect",
				breakProtectDisplay,
				downgradeProtectBase
		);
		//
		AdvancementDisplay boosterUseDisplay = new AdvancementDisplay(
				EnchantMaterial.BoosterTenPercent(),
				"피로 물든 종이",
				AdvancementFrameType.CHALLENGE,
				true, false,
				4, 3,
				"강화서의 도움을 받아 강해지세요."
		);
		BaseAdvancement boosterUserBase = new BaseAdvancement(
				"booster_use",
				boosterUseDisplay,
				breakProtectBase
		);
		globalTab.registerAdvancements(root, consumeBreadBase, torchFlowerPlantBase, sunFlowerBreadCreateBase
				, enchantBase, downgradeProtectBase, breakProtectBase, boosterUserBase);
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

			Component prefix = ColorUtils.chat("&f" + name + "이(가)");
			Component advance = ColorUtils.chat(" &b[" + title + "] ")
					.hoverEvent(HoverEvent.showText(ColorUtils.chat("&b" + display.getCompactDescription())));
			Component end = ColorUtils.chat("&f도전을 완료했습니다.");

			Bukkit.broadcast(prefix.append(advance).append(end));
			player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
		}
	}
}
