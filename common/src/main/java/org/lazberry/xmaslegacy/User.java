package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Roles;


import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID uuid;
	private String name;
    private @Nullable Roles role;
    private int dollars;
    private int inquireCount = 0;
    private int playTime = 0;
	private boolean isNewUser = false;
    private boolean wantsCookie = true;

    public User(@NotNull UUID uuid, @Nullable Roles role, @NotNull String name) {
        this.uuid = uuid;
		this.name = name;
        this.role = role;
    }

    public UUID getUUID() {return this.uuid;}
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
    public void wantsCookie(boolean wantsCookie) {this.wantsCookie = wantsCookie;}
    public boolean ifWantsCookie() {return this.wantsCookie;}
	public boolean isNewUser() {return this.isNewUser;}
	public void setNewUser(boolean isNewUser) {this.isNewUser = isNewUser;}
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}

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
