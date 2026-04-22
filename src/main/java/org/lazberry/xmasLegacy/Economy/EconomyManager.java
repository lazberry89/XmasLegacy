package org.lazberry.xmasLegacy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmasLegacy.Settings.Constants;
import org.lazberry.xmasLegacy.User.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final UserManager UM;
    private final Map<String, Integer> marketDemand = new HashMap<>();

    public EconomyManager(UserManager um) {
        this.UM = um;
    }

    public void deposit(Player p, int amount) {
        UM.deposit(p, amount);
    }

    public boolean deposit(UUID uuid, int amount) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            UM.deposit(p, amount);
            return true;
        }
        return false;
    }

    public boolean withdraw(Player p, int amount) {
        return UM.withdraw(p, amount);
    }

    public boolean withdraw(UUID uuid, int amount) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            return UM.withdraw(p, amount);
        }
        return false;
    }

    public boolean transferMoney(Player sender, Player target, int amount) {
        if (target == null || sender == null) return false;
        if (sender.getUniqueId().equals(target.getUniqueId())) return false;
        if (withdraw(sender, amount)) {
            deposit(target, amount * (100- Constants.TAX_RATE)/100);
            return true;
        }
        return false;
    }

    public boolean transferMoney(UUID uuid, UUID uuid1, int amount) {
        Player sender = Bukkit.getPlayer(uuid);
        Player target = Bukkit.getPlayer(uuid1);
        if (target == null || sender == null) return false;
        if (sender.getUniqueId().equals(target.getUniqueId())) return false;
        if (amount <= 0) return false;
        if (withdraw(sender, amount)) {
            deposit(target, amount * (100- Constants.TAX_RATE)/100);
            return true;
        }
        return false;
    }

    public int checkBalance(Player p) {
        return UM.getUser(p).getDollars();
    }

    public int checkBalance(UUID uuid) {
        return UM.getUser(uuid).getDollars();
    }

    public boolean hasEnough(Player p, int amount) {
        if (amount <= 0) return false;
        int userMoney = UM.getUser(p).getDollars();
        return userMoney >= amount;
    }

    public boolean hasEnough(UUID uuid, int amount) {
        if (amount <= 0) return false;
        int userMoney = UM.getUser(uuid).getDollars();
        return userMoney >= amount;
    }

    // 1. 누군가 물건을 샀을 때 판매량 증가 (ShopListener의 onTrade에서 호출하세요!)
    public void recordSale(String itemKey, int amount) {
        marketDemand.put(itemKey, marketDemand.getOrDefault(itemKey, 0) + amount);
    }

    // 2. 적게 사면 내려가게 만들기 위한 로직 (스케줄러로 주기적으로 호출)
    public void decayDemand() {
        for (String key : marketDemand.keySet()) {
            int current = marketDemand.get(key);
            if (current > 0) {
                marketDemand.put(key, Math.max(0, current - 1));
            }
        }
    }

    // 3. 현재 판매량에 비례한 가격 인상/할인 수치 반환 (Special Price용)
    public int getPriceAdjustment(String itemKey, int basePrice) {
        int soldCount = marketDemand.getOrDefault(itemKey, 0);

        // 예시 공식: 5개 팔릴 때마다 기본 가격의 10%씩 인상
        // 만약 할인도 적용하고 싶다면 soldCount의 기준점을 잡고 음수를 반환하도록 짜면 됩니다.
        double rate = ((double) soldCount / 5) * 0.10;

        return (int) (basePrice * rate);
    }
}
