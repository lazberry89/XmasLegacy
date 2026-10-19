package org.lazberry.xmaslegacy.airdrop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.currency.CurrencyManager;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropContentsManager implements Initiator {
	private final Set<AirdropContents> dropItems = ConcurrentHashMap.newKeySet();
	private final XmasLegacy plugin;

	@Inject
	public AirdropContentsManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {

		//Server contents of Survival
		dropItems.add(new AirdropContents(new ItemStack(Material.TORCHFLOWER), 1, 3, 0.2));
		dropItems.add(new AirdropContents(new ItemStack(Material.TORCHFLOWER_SEEDS), 1, 2, 0.4));
		dropItems.add(new AirdropContents(AgeableCrops.SunFlowerBread(), 1, 5, 0.6));

		//Economy
		dropItems.add(new AirdropContents(CurrencyManager.currency(plugin), 1, 3, 0.8));

		//Treasures
		dropItems.add(new AirdropContents(new ItemStack(Material.IRON_INGOT), 2, 5, 0.5));
		dropItems.add(new AirdropContents(new ItemStack(Material.GOLD_INGOT), 1, 3, 0.3));
		dropItems.add(new AirdropContents(new ItemStack(Material.DIAMOND), 1, 2, 0.15));
		dropItems.add(new AirdropContents(new ItemStack(Material.NETHERITE_SCRAP), 1, 1, 0.03));

		//Enchant
		dropItems.add(new AirdropContents(EnchantMaterial.PrismFractal(), 1, 5, 0.3));
	}

	public boolean add(ItemStack item, int amountMin, int amountMax, double chancePerSlot) {
		return dropItems.add(new AirdropContents(item, amountMin, amountMax, chancePerSlot));
	}

	public ItemStack getRandomAmountOfItem() {
		if (dropItems.isEmpty()) return null;

		var random = ThreadLocalRandom.current();

		double slotAppearanceChance = 0.3;
		if (random.nextDouble() > slotAppearanceChance) return null;

		double totalWeight = dropItems.stream()
				.mapToDouble(AirdropContents::chancePerSlot).sum();
		if (totalWeight <= 0) return null;

		double value = random.nextDouble() * totalWeight;
		double currentWeight = 0.0;

		AirdropContents selectedContent = null;
		for (AirdropContents content : dropItems) {
			currentWeight += content.chancePerSlot();
			if (value <= currentWeight) {
				selectedContent = content;
				break;
			}
		}

		if (selectedContent == null || selectedContent.item() == null) return null;

		int min = selectedContent.minimumAmount();
		int max = selectedContent.maximumAmount();
		int amount = min;

		if (max > min) {
			int range = (max - min) + 1;

			double[] weights = new double[range];
			double weightSum = 0.0;

			for (int i = 0; i < range; i++) {
				weights[i] = range - i;
				weightSum += weights[i];
			}

			double randomWeightValue = random.nextDouble() * weightSum;
			double currentW = 0.0;
			int chosenOffset = 0;

			for (int i = 0; i < range; i++) {
				currentW += weights[i];
				if (randomWeightValue <= currentW) {
					chosenOffset = i;
					break;
				}
			}

			amount = min + chosenOffset;
		}

		ItemStack resultItem = selectedContent.item().clone();
		resultItem.setAmount(amount);

		return resultItem;
	}

	@Override
	public void close() {
		dropItems.clear();
	}
}
