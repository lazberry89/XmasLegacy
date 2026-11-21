package org.lazberry.xmaslegacy.role.shop.role;

import lombok.Data;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.role.farmer.OraxenItemShop;
import org.lazberry.xmaslegacy.role.shop.role.farmer.guide.FarmerGuideBook;
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
	 *
	 * Price model: other jobs earn about 200-300k$/hour, so a working farm of 48 pots
	 * (two 5x5 top sprinkler fields) should earn ~250k$/hour, i.e. ~5,200$ per pot per hour.
	 * Measured 2026-11-22 (corn, 22 sprinkler-watered pots, no fertilizer): most were ripe at 6 minutes,
	 * a few took 9, so a 3-stage crop is ~6.5 minutes (~2.2 per stage): 5 stages ~11, a regrowing vine ~9.
	 * Normal price = value per harvest / (average yield x 1.25), where 1.25 is the average
	 * quality multiplier at the default 85/10/5% normal/silver/gold odds with silver x2 and gold x4.
	 * Seeds drop back on harvest, so seed prices are a start-up cost only.
	 */
	@Data
	public class Farmer {
		private final List<OraxenItemShop.Entry> tools = List.of(
				new OraxenItemShop.Entry("farmer_guide_book", 100, FarmerGuideBook::create),
				entry("dry_pot", 1_000), entry("greenhouse_glass", 500),
				entry("watering_can_1", 2_000), entry("watering_can_2", 8_000),
				entry("watering_can_3", 25_000), entry("watering_can_4", 60_000),
				entry("sprinkler_1_item", 10_000), entry("sprinkler_2_item", 30_000), entry("sprinkler_3_item", 80_000),
				entry("scarecrow", 15_000), entry("soil_surveyor", 5_000),
				entry("quality_1", 1_500), entry("quality_2", 3_500), entry("quality_3", 7_000),
				entry("yield_increase_1", 1_500), entry("yield_increase_2", 3_500), entry("yield_increase_3", 7_000),
				entry("speed_grow_1", 1_000), entry("speed_grow_2", 2_500), entry("speed_grow_3", 5_000),
				entry("soil_retain_1", 500), entry("soil_retain_2", 1_200), entry("soil_retain_3", 2_500),
				entry("variation_1", 1_000), entry("variation_2", 2_500), entry("variation_3", 5_000)
		);

		private final List<OraxenItemShop.Entry> seeds = List.of(
				entry("cabbage_seeds", 100), entry("chinese_cabbage_seeds", 100), entry("corn_seeds", 100),
				entry("eggplant_seeds", 100), entry("garlic_seeds", 100), entry("pineapple_seeds", 100),
				entry("hop_seeds", 200),
				entry("tomato_seeds", 150), entry("pepper_seeds", 150),
				entry("grape_seeds", 500), entry("pitaya_seeds", 500),
				entry("redpacket_seeds", 200)
		);

		private final List<OraxenItemShop.Entry> crops = Stream.of(
				// 3 stages, 1 per harvest
				qualities("cabbage", 450), qualities("chinese_cabbage", 450),
				qualities("corn", 450), qualities("pineapple", 450),
				// 3 stages, several per harvest
				qualities("eggplant", 130), qualities("hop", 130),
				qualities("garlic", 100), // 4 per harvest, any season and never withers
				// 5 stages
				qualities("tomato", 300), qualities("pepper", 165),
				// vines that regrow from stage 4
				qualities("grape", 150), qualities("pitaya", 140), // pitaya needs no water
				List.of(
						entry("golden_tomato", 4_000), // rare variation, needs a golden hoe
						entry("redpacket", 455)        // no quality, no seed drop: covers a 200$ seed each harvest
				)
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
