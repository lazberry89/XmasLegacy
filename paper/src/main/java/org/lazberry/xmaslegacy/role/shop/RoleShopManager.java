package org.lazberry.xmaslegacy.role.shop;

import lombok.Data;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.farmer.CropSellingShop;
import org.lazberry.xmaslegacy.role.shop.farmer.FarmPurchaseShop;
import org.lazberry.xmaslegacy.role.shop.farmer.FarmerSelectionGui;
import org.lazberry.xmaslegacy.role.shop.fisherman.FishSellingShop;
import org.lazberry.xmaslegacy.role.shop.miner.MinerSellingShop;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Exclude(type = ServerType.LOBBY)
public class RoleShopManager {
	private final Farmer farmer = new Farmer();
	private final Fisherman fisherman = new Fisherman();
	private final Miner miner = new Miner();
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

	@Data
	public class Farmer {
		private int wheatPrice = 35;
		private int carrotPrice = 30;
		private int potatoPrice = 30;
		private int bakedPotatoPrice = 40;
		private int netherWartPrice = 55;
		private int pumpkinPrice = 110;
		private int melonSlicePrice = 16;

		private int wheatSeedPrice = 15;
		private int wheatPriceForShop = 85;
		private int carrotPriceForShop = 75;
		private int potatoPriceForShop = 75;
		private int netherWartPriceForShop = 165;
		private int pumpkinSeedPrice = 400;
		private int pumpkinPriceForShop = 275;
		private int melonSeedPrice = 350;
		private int melonSlicePriceForShop = 40;

		private final FarmerSelectionGui selectionGui = new FarmerSelectionGui(plugin);
		private final CropSellingShop sellingShop = new CropSellingShop(plugin, this);
		private final FarmPurchaseShop purchaseShop = new FarmPurchaseShop(plugin, this);
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
}
