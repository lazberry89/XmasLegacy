package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class EconomyManager {
    private final @NotNull UserManager um;
    private final @NotNull Map<String, Integer> marketDemand = new HashMap<>();

	@Inject
    public EconomyManager(@NotNull UserManager um) {
		this.um = um;
    }

    public boolean deposit(@Nullable UUID uuid, int amount) {
        if (uuid != null) {
            um.deposit(uuid, amount);
            return true;
        }
        return false;
    }

    public boolean withdraw(@NotNull UUID uuid, int amount) {
        User user = um.getUser(uuid);
        if (user != null) {
            return um.withdraw(uuid, amount);
        }
        return false;
    }

    public boolean transferMoney(@NotNull UUID uuid, @NotNull UUID uuid1, int amount) {
        User sender = um.getUser(uuid);
        User target = um.getUser(uuid1);
        if (target == null || sender == null) return false;
        if (sender.equals(target)) return false;
        if (amount <= 0) return false;
        if (withdraw(sender.getUniqueId(), amount)) {
            deposit(target.getUniqueId(), amount * (100- Constants.TAX_RATE)/100);
            return true;
        }
        return false;
    }

    public int checkBalance(@NotNull UUID uuid) {
		User user = um.getUser(uuid);
	    return (user != null) ? user.getDollars() : 0;
    }

	public boolean setBalance(@NotNull UUID uuid, int amount) {
		User user = um.getUser(uuid);
		if (user == null) return false;
		user.setDollars(amount);
		return true;
	}

    public boolean hasEnough(@NotNull UUID uuid, int amount) {
        if (amount <= 0) return false;

		var user = um.getUser(uuid);
		if (user == null) return false;

        int userMoney = user.getDollars();
        return userMoney >= amount;
    }

    public void recordSale(@NotNull String itemKey, int amount) {
        marketDemand.put(itemKey, marketDemand.getOrDefault(itemKey, 0) + amount);
    }

    public void decayDemand() {
        for (String key : marketDemand.keySet()) {
            int current = marketDemand.get(key);
            if (current > 0) {
                marketDemand.put(key, Math.max(0, current - 1));
            }
        }
    }

    public int getPriceAdjustment(String itemKey, int basePrice) {
        int soldCount = marketDemand.getOrDefault(itemKey, 0);
        double rate = ((double) soldCount / 5) * 0.10;

        return (int) (basePrice * rate);
    }
}
