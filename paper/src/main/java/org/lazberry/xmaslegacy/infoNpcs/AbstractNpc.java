package org.lazberry.xmaslegacy.infoNpcs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.currency.CurrencyManager;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractNpc {
	protected final @NotNull @Getter NpcType type;
    private final @NotNull @Getter XmasLegacy plugin;
    protected final @NotNull Map<UUID, Integer> playerCaption = new HashMap<>();
    protected final @NotNull List<String> caption;
    private final @NotNull @Getter NamespacedKey key;
	private final @NotNull @Getter Component name;
	private final @NotNull Sound conversationSound;
	protected final @NotNull Map<UUID, Long> lastTalkTime = new HashMap<>();
	private final @Getter NamespacedKey checkKey;
	private final @Getter NamespacedKey foodKey;
	private static final long DIALOGUE_TIMEOUT = 20000L;

    public AbstractNpc(@NotNull List<String> cap, @NotNull Component name, @NotNull Sound conversationSound, @NotNull NpcType type) {
		this.plugin = XmasLegacy.getInstance();
		this.type = type;
        this.key = KeyUtils.get("npc");
        this.caption = cap;
		this.name = name;
		this.conversationSound = conversationSound;
		this.checkKey = KeyUtils.get("check");
		this.foodKey = KeyUtils.get("foodKey");
    }

	protected @NotNull String next(@NotNull Player player) {
		var uuid = player.getUniqueId();
		long currentTime = System.currentTimeMillis();

		if (this.lastTalkTime.containsKey(uuid)) {
			long lastTime = this.lastTalkTime.get(uuid);
			if (currentTime - lastTime > DIALOGUE_TIMEOUT) {
				this.playerCaption.put(uuid, 0);
			}
		}

		this.lastTalkTime.put(uuid, currentTime);

		int num = this.playerCaption.getOrDefault(uuid, 0);
		String currentCaption = this.caption.get(num);

		num++;
		if (num == 2 && NpcType.MAIN.equals(type)) provideFood(player);

		if (num >= this.caption.size()) {
			num = 0;
			this.lastTalkTime.remove(uuid);
			if (type == NpcType.MAIN) provideMoney(player);
		}

		this.playerCaption.put(uuid, num);
		return currentCaption;
	}

	private void provideMoney(@NotNull Player player) {
		if (catchKey(player, checkKey)) {
			ItemStack give = CurrencyManager.currency(plugin);
			give.setAmount(5);
			player.getInventory().addItem(give);
			InfoUtils.info(player, "재화를 클릭하여 현금 입금을 해보세요!");
		}
	}

	private void provideFood(@NotNull Player player) {
		if (catchKey(player, foodKey)) {
			ItemStack item = AgeableCrops.SunFlowerBread();
			item.setAmount(5);
			player.getInventory().addItem(item);
			InfoUtils.info(player, "태양초 음식이 제공되었습니다.");
			InfoUtils.warn(player, "관련 서적도 제공되었습니다. 필히 열람하십시오.");
			player.getInventory().addItem(Documents.IcingDocument());
		}
	}

	private boolean catchKey(@NotNull Player player, @NotNull NamespacedKey key) {
		var container = player.getPersistentDataContainer();
		if (!Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN))) {
			container.set(key, PersistentDataType.BOOLEAN, true);
			return true;
		}
		return false;
	}

    protected void sendCaption(@NotNull Player player) {
	    player.playSound(player, this.conversationSound, 1.0f, 1.0f);
	    Component txt = this.name.append(ColorUtils.chat(" &f" + next(player)));
	    player.sendActionBar(txt);
	    if (FloodgateUtils.isFloodgate(player.getUniqueId())) player.sendMessage(txt);
    }
}
