package org.lazberry.xmaslegacy.party;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class PartyManager implements Initiator {
	private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().create();

	public PartyManager() {}

	@Override
	public void init() {}

	private @Nullable Party getPartyById(@NotNull UUID partyId) {
		String json = JedisService.getFromJedis("party:" + partyId);
		return json == null ? null : GSON.fromJson(json, Party.class);
	}

	private void saveParty(@NotNull Party party) {
		JedisService.setToJedis("party:" + party.getPartyID(), GSON.toJson(party));
		for (UUID memberUuid : party.getMembers()) {
			setParty(memberUuid, party);
		}
	}

	public void removeUserPartyMapping(UUID uuid) {
		JedisService.setToJedis("user:" + uuid + ":party", null);
	}

	public boolean disbandParty(@NotNull UUID leaderUuid) {
		Party party = getParty(leaderUuid);
		if (party == null || !party.getLeader().equals(leaderUuid)) return false;

		deleteParty(party);
		return true;
	}

	private void deleteParty(@NotNull Party party) {
		for (UUID memberUuid : party.getMembers()) {
			removeUserPartyMapping(memberUuid);
		}
		JedisService.setToJedis("party:" + party.getPartyID(), null);
		party.clearMembers();
	}

	public void setParty(UUID uuid, Party party) {
		JedisService.setToJedis("user:" + uuid + ":party", party.getPartyID().toString());
	}

	public @Nullable Party getParty(@NotNull UUID uuid) {
		String partyIdStr = JedisService.getFromJedis("user:" + uuid + ":party");
		if (partyIdStr == null) return null;
		return getPartyById(UUID.fromString(partyIdStr));
	}

	public boolean isInParty(UUID uuid) {
		return JedisService.getFromJedis("user:" + uuid + ":party") != null;
	}

	public boolean createParty(@NotNull UUID leaderUuid) {
		if (isInParty(leaderUuid)) return false;

		Party newParty = new Party(leaderUuid);
		saveParty(newParty);
		return true;
	}

	public boolean joinParty(UUID leaderUuid, UUID joinUuid) {
		if (isInParty(joinUuid)) return false;

		String partyIdStr = JedisService.getFromJedis("user:" + leaderUuid + ":party");
		if (partyIdStr == null) return false;

		Party party = getPartyById(UUID.fromString(partyIdStr));
		if (party == null || !party.getLeader().equals(leaderUuid)) return false;

		if (party.joinParty(joinUuid)) {
			saveParty(party);
			return true;
		}
		return false;
	}

	public boolean leaveParty(UUID uuid) {
		Party party = getParty(uuid);
		if (party == null) return false;

		removeUserPartyMapping(uuid);

		boolean isStillAlive = party.leaveParty(uuid);

		if (!isStillAlive) {
			deleteParty(party);
		} else {
			saveParty(party);
		}
		return true;
	}

	public boolean isLeader(@NotNull UUID uuid) {
		Party party = getParty(uuid);
		if (party == null) return false;
		return party.getLeader().equals(uuid);
	}

	public boolean isParty(@NotNull UUID uuid1, @NotNull UUID uuid2) {
		Party party = getParty(uuid1);
		if (party == null) return false;

		return party.getMembers().stream()
				.anyMatch(user -> user.equals(uuid2));
	}

	public boolean handOverLeadership(@NotNull UUID currentLeaderUuid, @NotNull UUID newLeaderUuid) {
		Party party = getParty(currentLeaderUuid);
		if (party == null) return false;

		if (!party.getLeader().equals(currentLeaderUuid)) return false;
		return party.changeLeader(newLeaderUuid);
	}
}