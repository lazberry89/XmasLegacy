package org.lazberry.xmaslegacy.role.shop.role;

import lombok.Data;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.role.farmer.OraxenItemShop;
import org.lazberry.xmaslegacy.role.shop.role.farmer.FarmerSelectionGui;
import org.lazberry.xmaslegacy.role.shop.role.fisherman.FishSellingShop;
import org.lazberry.xmaslegacy.role.shop.role.hunter.HunterSellingShop;
import org.lazberry.xmaslegacy.role.shop.role.miner.MinerSellingShop;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.List;
import java.util.stream.Stream;

@Components.Exclude(type = ServerType.LOBBY)
public class RoleShopManager {
	private final Farmer farmer = new Farmer();
	private final Fisherman fisherman = new Fisherman();
	private final Miner miner = new Miner();
	private final Hunter hunter = new Hunter();
	private final XmasLegacy plugin;

	@Inject
	public RoleShopManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public Farmer farmer() {
		return farmer;
	}
	public Fisherman fisherman() {
		return fisherman;
	}
	public Miner miner() {
		return miner;
	}
	public Hunter hunter() {
		return hunter;
	}

	/**
	 * Farmer shops sell only CustomCrops items (Oraxen ids from plugins/Oraxen/items/customcrops).
	 * Vanilla crops are left to the server's normal economy.
	 */
	@Data
	public class Farmer {
		private final List<OraxenItemShop.Entry> tools = List.of(
				entry("watering_can_1", 500), entry("watering_can_2", 1_500),
				entry("watering_can_3", 4_000), entry("watering_can_4", 10_000),
				entry("sprinkler_1_item", 2_500), entry("sprinkler_2_item", 7_000), entry("sprinkler_3_item", 18_000),
				entry("dry_pot", 150), entry("greenhouse_glass", 60),
				entry("scarecrow", 2_000), entry("soil_surveyor", 1_000),
				entry("yield_increase_1", 300), entry("yield_increase_2", 900), entry("yield_increase_3", 2_500),
				entry("quality_1", 300), entry("quality_2", 900), entry("quality_3", 2_500),
				entry("soil_retain_1", 300), entry("soil_retain_2", 900), entry("soil_retain_3", 2_500),
				entry("speed_grow_1", 300), entry("speed_grow_2", 900), entry("speed_grow_3", 2_500),
				entry("variation_1", 300), entry("variation_2", 900), entry("variation_3", 2_500)
		);

		private final List<OraxenItemShop.Entry> seeds = List.of(
				entry("tomato_seeds", 40), entry("cabbage_seeds", 40), entry("chinese_cabbage_seeds", 40),
				entry("corn_seeds", 40), entry("eggplant_seeds", 40), entry("garlic_seeds", 40),
				entry("pepper_seeds", 40), entry("hop_seeds", 40),
				entry("grape_seeds", 80), entry("pineapple_seeds", 80), entry("pitaya_seeds", 80),
				entry("redpacket_seeds", 150)
		);

		private final List<OraxenItemShop.Entry> crops = Stream.of(
				qualities("tomato", 45), qualities("cabbage", 45), qualities("chinese_cabbage", 45),
				qualities("corn", 45), qualities("eggplant", 45), qualities("garlic", 45),
				qualities("pepper", 45), qualities("hop", 45),
				qualities("grape", 60), qualities("pineapple", 60), qualities("pitaya", 60),
				List.of(entry("golden_tomato", 600), entry("redpacket", 300))
		).flatMap(List::stream).toList();

		private final FarmerSelectionGui selectionGui = new FarmerSelectionGui(plugin);
		private final OraxenItemShop toolShop = new OraxenItemShop("농부 도구상점", OraxenItemShop.Mode.BUY, tools);
		private final OraxenItemShop seedShop = new OraxenItemShop("씨앗상점", OraxenItemShop.Mode.BUY, seeds);
		private final OraxenItemShop cropShop = new OraxenItemShop("작물판매", OraxenItemShop.Mode.SELL, crops);

		private static OraxenItemShop.Entry entry(String id, int price) {
			return new OraxenItemShop.Entry(id, price);
		}

		/** Normal, silver star and golden star quality of one crop, each worth double the one before. */
		private static List<OraxenItemShop.Entry> qualities(String crop, int base) {
			return List.of(entry(crop, base), entry(crop + "_silver_star", base * 2), entry(crop + "_golden_star", base * 4));
		}
	}

	@Data
	public class Fisherman {
		private int codPrice = 1000;
		private int cookedCodPrice = 1250;
		private int salmonPrice = 1600;
		private int cookedSalmonPrice = 2000;
		private int tropicalFishPrice = 2500;
		private int pufferfishPrice = 3500;
		private int nautilusShellPrice = 8000;

		private final FishSellingShop fishSellingShop = new  FishSellingShop(plugin, this);
	}

	@Data
	public class Miner {
		private int cobbleStonePrice = 2;
		private int cobbledDeepslatePrice = 3;
		private int coalPrice = 150;
		private int redstonePrice = 120;
		private int lapisPrice = 120;
		private int quartzPrice = 250;
		private int rawIronPrice = 600;
		private int ironPrice = 750;
		private int rawGoldPrice = 1000;
		private int goldPrice = 1250;
		private int diamondPrice = 5000;
		private int emeraldPrice = 8000;
		private int netheriteScrapPrice = 22000;
		private int netheriteIngotPrice = 100000;

		private final MinerSellingShop minerSellingShop = new  MinerSellingShop(plugin, this);
	}

	@Data
	public class Hunter {
		private int rottenFleshPrice = 40;
		private int bonePrice = 70;
		private int arrowPrice = 70;
		private int stringPrice = 80;
		private int spiderEyePrice = 80;
		private int gunpowderPrice = 90;
		private int slimeBallPrice = 130;
		private int phantomMembranePrice = 220;
		private int enderPearlPrice = 250;
		private int magmaCreamPrice = 350;
		private int witherSkeletonSkullPrice = 3500;
		private int blazeRodPrice = 600;
		private int ghastTearPrice = 1200;
		private int prismarineShardPrice = 180;

		private final HunterSellingShop sellingShop = new HunterSellingShop(plugin, this);
	}
}
