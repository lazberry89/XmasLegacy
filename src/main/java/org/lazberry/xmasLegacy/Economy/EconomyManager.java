package org.lazberry.xmasLegacy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmasLegacy.Settings.Constants;
import org.lazberry.xmasLegacy.User.UserManager;

import java.util.UUID;

public class EconomyManager {
    private final UserManager UM;

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
            deposit(target, amount * (100- Constants.TaxRate)/100);
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
            deposit(target, amount * (100- Constants.TaxRate)/100);
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
}
