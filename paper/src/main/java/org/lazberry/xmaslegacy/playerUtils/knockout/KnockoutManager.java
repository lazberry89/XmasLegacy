package org.lazberry.xmaslegacy.playerUtils.knockout;

import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Registry.Exclude(type = ServerType.LOBBY)
public class KnockoutManager {
	private final Map<UUID, KnockoutPlayer> knockoutPlayers = new ConcurrentHashMap<>();
	private final PartyManager pm;

	private int reviveCount = 5;
	private double reviveHealth = 3.0;

	@Inject
	public KnockoutManager(PartyManager pm) {
		this.pm = pm;
	}

	protected Collection<UUID> getKnockoutPlayerId() {
		return Collections.unmodifiableCollection(knockoutPlayers.keySet());
	}

	protected Collection<KnockoutPlayer> getKnockoutPlayers() {
		return Collections.unmodifiableCollection(knockoutPlayers.values());
	}

	public void knockdownPlayer(@Nullable Player player) {
		if (player == null || !player.isOnline() || !player.isValid()) return;
		var uuid = player.getUniqueId();
		if (!pm.isInParty(uuid)) return;

		KnockoutPlayer kp = knockoutPlayers.computeIfAbsent(uuid,
				u -> new KnockoutPlayer(player, reviveCount, reviveHealth));
		InfoUtils.warn(player, "기절했습니다! &6({}/{})", kp.getReviveCount(), reviveCount);
	}

	public void clickProcess(Player helper, Player downed) {
		var helperUuid = helper.getUniqueId();
		var downedUuid = downed.getUniqueId();

		if (pm.isParty(helperUuid, downedUuid)) {
			KnockoutPlayer kp = knockoutPlayers.get(downedUuid);
			if (kp == null) {
				InfoUtils.error(helper, "기절하지 않은 팀원입니다!");
				return;
			}
			switch (kp.touch()) {
				case TOO_FAST -> helper.sendActionBar(ColorUtils.chat(Alert.RED + "너무 빠릅니다!"));
				case PROGRESS -> {
					int progress = reviveCount - kp.getReviveCount();
					helper.sendActionBar(ColorUtils.chat(Alert.YELLOW + "&a소생 중... (" + progress + "/" + reviveCount + ")"));
					downed.sendActionBar(ColorUtils.chat(Alert.YELLOW + "&6" + helper.getName() + "&f님이 소생 중... (" + progress + "/" + reviveCount + ")"));
				}
				case REVIVED -> {
					knockoutPlayers.remove(downedUuid);
					InfoUtils.info(helper, "&6&l" + downed.getName() + "&f&r님을 소생시켰습니다!");
					InfoUtils.info(downed, "&6&l" + helper.getName() + "&f&r님에 의해 소생되었습니다!");
				}
			}
		} else {
			InfoUtils.error(helper, "같은 파티원만 소생할 수 있습니다.");
		}
	}
}
