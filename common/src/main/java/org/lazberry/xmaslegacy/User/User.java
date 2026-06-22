package org.lazberry.xmaslegacy.User;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
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
    private @NotNull Tier tier = Tier.VISITOR;
    private @NotNull RoleMastery mastery = RoleMastery.BEGINNER;
    private boolean isMobile = false;
	private boolean isNewUser = false;
	private boolean wantsCookie = true;
	private final @NotNull List<ServerPrefix> availablePrefix = new ArrayList<>();
	private @Nullable ServerPrefix equipPrefix;
	private boolean combatMode = false;
    private boolean isImmuneToIcing = false;

    public User(@NotNull UUID uuid, @Nullable Role role, @NotNull String name) {
        this.uuid = uuid;
		this.name = name;
        this.role = (role != null) ? role : BasicRoles.USER;
        this.availablePrefix.add(org.lazberry.xmaslegacy.settings.Tier.VISITOR);
    }

    public @NotNull UUID getUUID() {return this.uuid;}
    public @NotNull Role getRole() {return this.role;}
    public void setRole(@NotNull Role role) {this.role = role;}
    public void addDollars(int dollars) {
		this.dollars += dollars;
		if (this.dollars < 0) this.dollars = 0;
	}
    public void addInquireCount(int inquireCount) {this.inquireCount += inquireCount;}
    public void addPlayTime(int playTime) {this.playTime += playTime;}
    public void wantsCookie(boolean wantsCookie) {this.wantsCookie = wantsCookie;}
    public boolean ifWantsCookie() {return this.wantsCookie;}
	public @NotNull String getName() {return this.name;}
	public void setName(@NotNull String name) {this.name = name;}
    public void addExp(double amount) {this.exp += amount;}
    public boolean hasRole() {return !BasicRoles.USER.equals(this.role);}
    public void addRoleExp(double amount) {this.roleExp += amount;}
    public void addLevel(int amount) {this.level += amount;}
    public @NotNull Tier getTier() {return this.tier;}
    public void setTier(@NotNull Tier tier) {this.tier = tier;}
    public @NotNull RoleMastery getMastery() {return mastery;}
    public void setMastery(@NotNull RoleMastery mastery) {this.mastery = mastery;}
	public boolean addPrefix(@NotNull ServerPrefix prefix) {
        if (this.availablePrefix.contains(prefix)) return false;
        this.availablePrefix.add(prefix);
        return true;
    }
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
	public boolean getCombatValue() {return this.combatMode;}

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
