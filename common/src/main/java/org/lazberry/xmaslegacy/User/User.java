package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;


import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class User {
    private final UUID uuid;
	private @NotNull String name;
    private @NotNull Role role;
    private int dollars = 0;
    private int inquireCount = 0;
    private int playTime = 0;
    private double exp = 0.0f;
    private double roleExp = 0.0f;
    private int level = 0;
    private Tier Tier = org.lazberry.xmaslegacy.settings.Tier.VISITOR;
    private RoleMastery mastery = RoleMastery.BEGINNER;
    private boolean isMobile = false;
	private boolean isNewUser = false;
    private boolean wantsCookie = true;
	private List<ServerPrefix> availablePrefix;
	private @Nullable ServerPrefix equipPrefix;

    public User(@NotNull UUID uuid, @Nullable Role role, @NotNull String name) {
        this.uuid = uuid;
		this.name = name;
        this.role = (role != null) ? role : Roles.USER;
    }

    public UUID getUUID() {return this.uuid;}
    public @NotNull Role getRole() {return this.role;}
    public int getDollars() {return this.dollars;}
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
	public @NotNull String getName() {return this.name;}
	public void setName(@NotNull String name) {this.name = name;}
    public void setMobile(boolean mobile) {this.isMobile = mobile;}
    public boolean isMobile() {return this.isMobile;}
    public void addExp(double amount) {this.exp += amount;}
    public void setExp(double amount) {this.exp = amount;}
    public double getExp() {return this.exp;}
    public boolean hasRole() {return !Roles.USER.equals(this.role);}
    public void addRoleExp(double amount) {this.roleExp += amount;}
    public void setRoleExp(double amount) {this.roleExp = amount;}
    public double getRoleExp() {return this.roleExp;}
    public void addLevel(int amount) {this.level += amount;}
    public void setLevel(int amount) {this.level = amount;}
    public int getLevel() {return this.level;}
    public @NotNull Tier getTier() {return this.Tier;}
    public void setTier(@NotNull Tier tier) {this.Tier = tier;}
    public @NotNull RoleMastery getMastery() {return mastery;}
    public void setMastery(@NotNull RoleMastery mastery) {this.mastery = mastery;}
	public boolean addPrefix(@NotNull ServerPrefix prefix) {
        if (this.availablePrefix.contains(prefix)) return false;
        this.availablePrefix.add(prefix);
        return true;
    }
	public List<ServerPrefix> getAvailablePrefix() {return this.availablePrefix;}
	public boolean removePrefix(@NotNull ServerPrefix prefix) {
        if (!this.availablePrefix.contains(prefix)) return false;
        this.availablePrefix.remove(prefix);
        return true;
    }
	public @Nullable ServerPrefix getEquipPrefix() {return this.equipPrefix;}
	public void setEquipPrefix(@Nullable ServerPrefix prefix) {this.equipPrefix = prefix;}
	public boolean removeEquipped() {
		if (this.equipPrefix == null) return false;
		this.equipPrefix = null;
		return true;
	}

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
