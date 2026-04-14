package org.lazberry.xmasLegacy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Roles.Roles;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private String name;
    private @Nullable Roles role;
    private int dollars;
    private int inquireCount;
    private int playTime;

    public User(@NotNull Player p, @Nullable Roles role) {
        this.uuid = p.getUniqueId();
        this.name = p.getName();
        this.role = role;
        if (!p.hasPlayedBefore()) this.dollars = p.getName().startsWith(".") ? 5000 : 1000;
    }

    public UUID getUUID() {return this.uuid;}
    public String getName() {return this.name;}
    public Roles getRole() {return this.role;}
    public Integer getDollars() {return this.dollars;}
    public int getInquireCount() {return this.inquireCount;}
    public int getPlayTime() {return this.playTime;}
    public void setDollars(int dollars) {this.dollars = dollars;}
    public void setInquireCount(int inquireCount) {this.inquireCount = inquireCount;}
    public void setPlayTime(int playTime) {this.playTime = playTime;}
    public void setRole(Roles role) {this.role = role;}
    public void addDollars(int dollars) {this.dollars += dollars;}
    public void addInquireCount(int inquireCount) {this.inquireCount += inquireCount;}
    public void addPlayTime(int playTime) {this.playTime += playTime;}
    public boolean isMobile() {return this.name.startsWith(".");}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User c)) return false;
        return this.uuid.equals(c.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
