package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;


import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID uuid;
	private String name;
    private @NotNull Role role;
    private int dollars;
    private int inquireCount = 0;
    private int playTime = 0;
    private double exp = 0.0f;
    private boolean isMobile = false;
	private boolean isNewUser = false;
    private boolean wantsCookie = true;

    public User(@NotNull UUID uuid, @Nullable Role role, @NotNull String name) {
        this.uuid = uuid;
		this.name = name;
        this.role = (role != null) ? role : Roles.USER;
    }

    public UUID getUUID() {return this.uuid;}
    public @NotNull Role getRole() {return this.role;}
    public Integer getDollars() {return this.dollars;}
    public int getInquireCount() {return this.inquireCount;}
    public int getPlayTime() {return this.playTime;}
    public void setDollars(int dollars) {this.dollars = dollars;}
    public void setInquireCount(int inquireCount) {this.inquireCount = inquireCount;}
    public void setPlayTime(int playTime) {this.playTime = playTime;}
    public void setRole(@NotNull Role role) {this.role = role;}
    public void addDollars(int dollars) {
		this.dollars += dollars;
		if (this.dollars < 0) this.dollars = 0;
	}
    public void addInquireCount(int inquireCount) {this.inquireCount += inquireCount;}
    public void addPlayTime(int playTime) {this.playTime += playTime;}
    public void wantsCookie(boolean wantsCookie) {this.wantsCookie = wantsCookie;}
    public boolean ifWantsCookie() {return this.wantsCookie;}
	public boolean isNewUser() {return this.isNewUser;}
	public void setNewUser(boolean isNewUser) {this.isNewUser = isNewUser;}
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
    public void setMobile(boolean mobile) {this.isMobile = mobile;}
    public boolean isMobile() {return this.isMobile;}
    public void addExp(double amount) {this.exp += amount;}
    public void setExp(double amount) {this.exp = amount;}
    public double getExp() {return this.exp;}

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
