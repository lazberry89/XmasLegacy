package org.lazberry.xmaslegacy;

import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final UserManager um;
    private final Map<String, Integer> marketDemand = new HashMap<>();
    private static EconomyManager instance;

    public static EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }

    public EconomyManager() {
        this.um = UserManager.getInstance();
    }

    public boolean deposit(UUID uuid, int amount) {
		User user = um.getUser(uuid);
        if (uuid != null) {
            um.deposit(uuid, amount);
            return true;
        }
        return false;
    }

    public boolean withdraw(UUID uuid, int amount) {
        User user = um.getUser(uuid);
        if (user != null) {
            return um.withdraw(uuid, amount);
        }
        return false;
    }

    public boolean transferMoney(UUID uuid, UUID uuid1, int amount) {
        User sender = um.getUser(uuid);
        User target = um.getUser(uuid1);
        if (target == null || sender == null) return false;
        if (sender.equals(target)) return false;
        if (amount <= 0) return false;
        if (withdraw(sender.getUUID(), amount)) {
            deposit(target.getUUID(), amount * (100- Constants.TAX_RATE)/100);
            return true;
        }
        return false;
    }

    public int checkBalance(UUID uuid) {
		User user = um.getUser(uuid);
	    return (user != null) ? user.getDollars() : 0;
    }

	public boolean setBalance(UUID uuid, int amount) {
		User user = um.getUser(uuid);
		if (user == null) return false;
		user.setDollars(amount);
		return true;
	}

    public boolean hasEnough(UUID uuid, int amount) {
        if (amount <= 0) return false;
        int userMoney = um.getUser(uuid).getDollars();
        return userMoney >= amount;
    }

    public void recordSale(String itemKey, int amount) {
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
