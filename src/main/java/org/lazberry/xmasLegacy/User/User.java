package org.lazberry.xmasLegacy.User;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.Settings.Constants;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private final String name;
    private final Player p;
    private @Nullable Roles role;
    private int dollars;
    private int inquireCount = 0;
    private int playTime = 0;
    private boolean wantsCookie = true;

    public User(@NotNull Player p, @Nullable Roles role) {
        this.p = p;
        this.uuid = p.getUniqueId();
        this.name = p.getName();
        this.role = role;
        if (!p.hasPlayedBefore()) this.dollars = p.getName().startsWith(".") ? Constants.BASIC_MONEY_MOBILE : Constants.BASIC_MONEY_NORMAL;
    }

    public UUID getUUID() {return this.uuid;}
    public String getName() {return this.name;}
    public @Nullable Roles getRole() {return this.role;}
    public Integer getDollars() {return this.dollars;}
    public int getInquireCount() {return this.inquireCount;}
    public int getPlayTime() {return this.playTime;}
    public void setDollars(int dollars) {this.dollars = dollars;}
    public void setInquireCount(int inquireCount) {this.inquireCount = inquireCount;}
    public void setPlayTime(int playTime) {this.playTime = playTime;}
    public void setRole(@Nullable Roles role) {this.role = role;}
    public void addDollars(int dollars) {this.dollars += dollars;}
    public void addInquireCount(int inquireCount) {this.inquireCount += inquireCount;}
    public void addPlayTime(int playTime) {this.playTime += playTime;}
    public boolean isMobile() {return this.name.startsWith(".");}
    public void wantsCookie(boolean wantsCookie) {this.wantsCookie = wantsCookie;}
    public boolean ifWantsCookie() {return this.wantsCookie;}
    public UUID getUuid() {return this.uuid;}
    public Player getPlayer() {return this.p;}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User c)) return false;
        return Objects.equals(uuid, c.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
