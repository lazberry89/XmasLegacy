package org.lazberry.xmaslegacy.user;

import com.google.gson.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;
import org.lazberry.xmaslegacy.framework.annotation.Document;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(description = """
		Class for saving detail & info for user.
		Using Equals and HashCode override, made UserSaveManager or another DB access
		more easier.Also when editing this class's field, you should also change DB,
		emergency dump class's saving method.
		""")
public class User {
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Role.class, (JsonSerializer<Role>) (src, typeOfSrc, context) ->
					new JsonPrimitive(src.name())
			)
			.registerTypeAdapter(Role.class, (JsonDeserializer<Role>) (json, typeOfT, context) ->
					Role.parseRole(json.getAsString(), ServerRoles.USER))
			.registerTypeAdapter(ServerPrefix.class, (JsonSerializer<ServerPrefix>) (src, typeOfSrc, context) ->
					new JsonPrimitive(src.name())
			)
			.registerTypeAdapter(ServerPrefix.class, (JsonDeserializer<ServerPrefix>) (json, typeOfT, context) -> ServerPrefix.parse(json.getAsString(), Tier.VISITOR))
			.create();

    @EqualsAndHashCode.Include
    private final UUID uniqueId;
	private final String lock;
	private String name;
    private Role role;
	private long dollars = 0;
	private int inquireCount = 0;
	private long playTime = 0;
	private int exp = 0;
	private int roleExp = 0;
	private int sinPoints = 0;
	private int level = 0;
    private Tier tier = Tier.VISITOR;
    private RoleMastery mastery = RoleMastery.BEGINNER;
    private boolean isMobile = false;
	private boolean isNewUser = false;
	private boolean wantsCookie = true;
	private List<ServerPrefix> availablePrefix = new ArrayList<>(List.of(Tier.VISITOR, Tier.USER, RoleMastery.BEGINNER));
	private @Nullable ServerPrefix equipPrefix = Tier.VISITOR;
    private boolean isImmuneToIcing = false;
	private int icingState = 100;
	private boolean showBoard = true;

    public User(UUID uuid, Role role, String name) {
        this.uniqueId = uuid;
		this.lock = uuid.toString().intern();
		this.role = role;
		this.name = name;
    }

	public String toJson() {
		return GSON.toJson(this);
	}

	public static User fromJson(String json) {
		if (json == null || json.isEmpty()) return null;
		return GSON.fromJson(json, User.class);
	}

	public void addSinPoints(int amount) {
		this.sinPoints = Math.clamp(this.sinPoints + amount, 0, 100);
	}
	public void reduceSinPoints(int amount) {
		this.sinPoints = Math.max(0, this.sinPoints - amount);
	}
	public void addIcingState(int icingState) {
		this.icingState = Math.clamp(this.icingState + icingState, 0, 100);
	}
	public void setIcingState(int icingState) {
		this.icingState = Math.clamp(icingState, 0, 100);
	}
    public void addDollars(long dollars) {
		this.dollars += dollars;
		if (this.dollars < 0) this.dollars = 0;
	}
    public void addInquireCount(int inquireCount) {this.inquireCount += inquireCount;}
    public void addPlayTime(long playTime) {this.playTime += playTime;}
    public void wantsCookie(boolean wantsCookie) {this.wantsCookie = wantsCookie;}
    public boolean ifWantsCookie() {return this.wantsCookie;}
    public void addExp(int amount) {this.exp += amount;}
    public boolean hasRole() {return !ServerRoles.USER.equals(this.role);}
    public void addRoleExp(int amount) {this.roleExp += amount;}
    public void addLevel(int amount) {this.level += amount;}
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
	public boolean removeEquipped() {
		if (this.equipPrefix == null) return false;
		this.equipPrefix = null;
		return true;
	}
}
